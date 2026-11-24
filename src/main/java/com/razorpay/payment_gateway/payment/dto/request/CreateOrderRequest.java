package com.razorpay.payment_gateway.payment.dto.request;

import com.razorpay.payment_gateway.common.entity.Money;

import java.time.Instant;
import java.util.Map;

public record CreateOrderRequest(
        Money money,

        String receipt,
        Map<String, Object> notes,
        Instant expiresAt
) {
}
