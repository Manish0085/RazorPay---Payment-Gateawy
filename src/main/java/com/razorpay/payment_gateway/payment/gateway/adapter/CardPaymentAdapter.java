package com.razorpay.payment_gateway.payment.gateway.adapter;

import com.razorpay.payment_gateway.payment.gateway.PaymentAdapter;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentResult;

public class CardPaymentAdapter implements PaymentAdapter {

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        return null;
    }
}
