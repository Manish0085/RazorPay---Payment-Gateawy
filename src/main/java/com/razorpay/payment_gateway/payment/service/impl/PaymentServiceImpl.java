package com.razorpay.payment_gateway.payment.service.impl;

import com.razorpay.payment_gateway.common.enums.OrderStatus;
import com.razorpay.payment_gateway.common.enums.PaymentStatus;
import com.razorpay.payment_gateway.common.exception.BusinessRuleViolationException;
import com.razorpay.payment_gateway.common.exception.ResourceNotFoundException;
import com.razorpay.payment_gateway.payment.dto.request.PaymentInitRequest;
import com.razorpay.payment_gateway.payment.dto.response.PaymentResponse;
import com.razorpay.payment_gateway.payment.entity.OrderRecord;
import com.razorpay.payment_gateway.payment.entity.Payment;
import com.razorpay.payment_gateway.payment.gateway.PaymentGatewayRouter;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentResult;
import com.razorpay.payment_gateway.payment.mapper.PaymentMapper;
import com.razorpay.payment_gateway.payment.repository.OrderRepository;
import com.razorpay.payment_gateway.payment.repository.PaymentRepository;
import com.razorpay.payment_gateway.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private final PaymentGatewayRouter gatewayRouter;

    private final PaymentMapper paymentMapper;
    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request){
        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order with id " + request.orderId() + " not found"));

        if (order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED) {
            throw new BusinessRuleViolationException("INVALID_ORDER_STATE", "Cannot initiate payment for order status " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);

        Payment payment = Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .paymentStatus(PaymentStatus.CREATED)
                .method(request.method())
                .methodDetails(request.methodDetails())
                .build();



        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(
                payment.getId(),
                request.orderId(),
                merchantId,
                order.getAmount(),
                request.method(),
                request.methodDetails()
        );
        PaymentResult result = gatewayRouter.initiate(paymentRequest);

//
//        if (result instanceof PaymentResult.Pending) {
//            payment.setProcessorReference(payment.getProcessorReference());
//        } else if (result instanceof PaymentResult.Failure failure) {
//            payment.setPaymentStatus(PaymentStatus.FAILED);
//            payment.setErrorCode(failure.errorCode());
//            payment.setErrorDescription(failure.description());
//        }
        switch (request) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure -> {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.description());
            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentMapper.toResponse(payment);
    }
}
