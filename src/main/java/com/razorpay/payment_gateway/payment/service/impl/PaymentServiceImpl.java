package com.razorpay.payment_gateway.payment.service.impl;

import com.razorpay.payment_gateway.common.enums.OrderStatus;
import com.razorpay.payment_gateway.common.enums.PaymentEvent;
import com.razorpay.payment_gateway.common.enums.PaymentStatus;
import com.razorpay.payment_gateway.common.exception.BusinessRuleViolationException;
import com.razorpay.payment_gateway.common.exception.ResourceNotFoundException;
import com.razorpay.payment_gateway.payment.dto.request.PaymentInitRequest;
import com.razorpay.payment_gateway.payment.dto.response.PaymentResponse;
import com.razorpay.payment_gateway.payment.entity.OrderRecord;
import com.razorpay.payment_gateway.payment.entity.Payment;
import com.razorpay.payment_gateway.payment.entity.PaymentTransitionLog;
import com.razorpay.payment_gateway.payment.gateway.PaymentGatewayRouter;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentResult;
import com.razorpay.payment_gateway.payment.mapper.PaymentMapper;
import com.razorpay.payment_gateway.payment.repository.OrderRepository;
import com.razorpay.payment_gateway.payment.repository.PaymentRepository;
import com.razorpay.payment_gateway.payment.service.PaymentService;
import com.razorpay.payment_gateway.payment.statemachine.PaymentTransitionService;
import jdk.dynalink.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentServiceImpl implements PaymentService {

    OrderRepository orderRepository;
    PaymentRepository paymentRepository;

    PaymentGatewayRouter gatewayRouter;

    PaymentMapper paymentMapper;

    PaymentTransitionService paymentTransitionService;

    PaymentGatewayRouter paymentGatewayRouter;


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

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
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
//                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.description());
            }
            case PaymentResult.Success success-> {

            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        // TODO: send an outbox (kafka event)
        return paymentMapper.toResponse(payment);
    }

    @Override

    public PaymentResponse capture(UUID merchantId, UUID paymentId) {

        Payment payment  = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(()-> new ResourceNotFoundException("PAYMENT_NOT_FOUND", "Payment not found with id: " + paymentId));


        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
        PaymentResult result = gatewayRouter.capture(payment.getMethod(), paymentId);

        if (result instanceof PaymentResult.Success success) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(Instant.now());
            log.info("Payment captured, paymentId: {}", paymentId);

        } else if (result instanceof PaymentResult.Failure failure) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.description());
            log.warn("Payment capture Failed, paymentId: {}", paymentId);
        }

        payment = paymentRepository.save(payment);
        // TODO: send an outbox (kafka event)

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND", "Payment with Id " + paymentId + " not found"));

        if (payment.getPaymentStatus() != PaymentStatus.AUTHORIZING) {
            log.warn("Payment is not in the Authorizing state, paymentId {}, status {}", paymentId, payment.getPaymentStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();
        if (approve) {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(Instant.now());

            // Auto Capture
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

            if (captureResult instanceof PaymentResult.Success success) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(Instant.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);
            } else if (captureResult instanceof PaymentResult.Failure failure) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.description());
            }

        } else {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }
        paymentRepository.save(payment);
        orderRepository.save(orderRecord);

        // TODO: send an outbox (kafka event)
    }
}
