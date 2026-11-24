package com.razorpay.payment_gateway.merchant.service;

import com.razorpay.payment_gateway.merchant.dto.request.CreateApiKeyRequest;
import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyCreateResponse;

import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);
}
