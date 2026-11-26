package com.razorpay.payment_gateway.payment.service;

import com.razorpay.payment_gateway.payment.dto.request.PaymentInitRequest;
import com.razorpay.payment_gateway.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiate(UUID merchantId, PaymentInitRequest request);

    PaymentResponse capture(UUID merchantId, UUID paymentId);
}
