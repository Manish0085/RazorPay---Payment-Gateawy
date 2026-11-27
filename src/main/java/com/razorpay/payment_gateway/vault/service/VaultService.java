package com.razorpay.payment_gateway.vault.service;

import com.razorpay.payment_gateway.vault.dto.request.TokenizeRequest;
import com.razorpay.payment_gateway.vault.dto.response.TokenizeResponse;

import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);
}
