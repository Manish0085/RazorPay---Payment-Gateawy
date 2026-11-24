package com.razorpay.payment_gateway.merchant.service;

import com.razorpay.payment_gateway.merchant.dto.response.MerchantResponse;
import com.razorpay.payment_gateway.merchant.dto.request.MerchantSignupRequest;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);
}
