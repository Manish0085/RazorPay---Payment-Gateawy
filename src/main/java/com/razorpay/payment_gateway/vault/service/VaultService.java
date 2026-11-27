package com.razorpay.payment_gateway.vault.service;

import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorResponse;
import com.razorpay.payment_gateway.vault.dto.request.TokenizeRequest;
import com.razorpay.payment_gateway.vault.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);

}
