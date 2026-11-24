package com.razorpay.payment_gateway.payment.dto.response;

import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.OrderStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID merchantId,
    String receipt,
    Money money,
    OrderStatus status,
    Integer attempts,
    Map<String, Object> notes,
    Instant expiresAt,
    Instant createdAt
) {
}
