package com.razorpay.payment_gateway.payment.service.impl;

import com.razorpay.payment_gateway.common.enums.OrderStatus;
import com.razorpay.payment_gateway.common.exception.DuplicateResourceException;
import com.razorpay.payment_gateway.payment.dto.request.CreateOrderRequest;
import com.razorpay.payment_gateway.payment.dto.response.OrderResponse;
import com.razorpay.payment_gateway.payment.entity.OrderRecord;
import com.razorpay.payment_gateway.payment.repository.OrderRepository;
import com.razorpay.payment_gateway.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Value("${payment.order.default-order-expiry-seconds:1800}")
    private final int defaultOrderExpirySeconds;
    @Override
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if (request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists " + request.receipt());
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.money())
                .notes(request.notes())
                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null? request.expiresAt(): Instant.now().plusSeconds(defaultOrderExpirySeconds))
                .build();
        return new OrderResponse(order.getId(), order.getMerchantId(),
                order.getReceipt(), order.getAmount(),
                order.getOrderStatus(), order.getAttempts(), order.getNotes(),
                order.getExpiresAt(), null);
    }
}
