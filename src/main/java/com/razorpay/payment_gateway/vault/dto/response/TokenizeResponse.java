package com.razorpay.payment_gateway.vault.dto.response;

import com.razorpay.payment_gateway.common.enums.CardBrand;

public record TokenizeResponse(

        String token,
        String lastFour,
        CardBrand cardBrand,
        Integer expiryMonth,
        Integer expiryYear
) {
}
