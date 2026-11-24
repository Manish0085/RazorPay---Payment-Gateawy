package com.razorpay.payment_gateway.merchant.dto.response;

import com.razorpay.payment_gateway.common.enums.Environment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String keyId,
        Environment environment,
        boolean enabled,
        Instant lastUsedAt,
        Instant createdAt
) {
}
