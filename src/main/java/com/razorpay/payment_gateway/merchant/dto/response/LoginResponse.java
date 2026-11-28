package com.razorpay.payment_gateway.merchant.dto.response;

import com.razorpay.payment_gateway.common.enums.BusinessType;
import com.razorpay.payment_gateway.common.enums.MerchantStatus;

import java.util.UUID;

public record LoginResponse(

        String token
) {
}
