package com.razorpay.payment_gateway.payment.processor.dto;

import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.PaymentMethod;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record PaymentProcessorRequest(
        PaymentMethod method,
        Money amount,
//        String pan,
//        String expiry,
        Map<String, Object> methodDetails
) {

    
}
