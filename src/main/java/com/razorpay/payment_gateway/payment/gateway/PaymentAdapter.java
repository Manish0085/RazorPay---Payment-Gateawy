package com.razorpay.payment_gateway.payment.gateway;

import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;

public interface PaymentAdapter {

    void initiate(PaymentRequest request);
}
