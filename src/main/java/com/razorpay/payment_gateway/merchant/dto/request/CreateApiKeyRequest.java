package com.razorpay.payment_gateway.merchant.dto.request;

import com.razorpay.payment_gateway.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
