package com.razorpay.payment_gateway.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.PaymentMethod;
import com.razorpay.payment_gateway.common.enums.PaymentStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        UUID id,
        UUID merchantId,
        UUID orderId,
        Money money,
        PaymentStatus status,
        PaymentMethod method,
        Map<String, Object> methodData,
        String cardLastFour,
        String cardBrand,
        String bankReference,
        String errorCode,
        String errorDescription,
        Long refundAmountPaise,
        Instant capturedAt,
        Instant createdAt
) {
}
