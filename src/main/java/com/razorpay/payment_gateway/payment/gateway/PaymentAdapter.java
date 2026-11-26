package com.razorpay.payment_gateway.payment.gateway;

import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentResult;

public interface PaymentAdapter {

    PaymentResult initiate(PaymentRequest request);
}
