package com.razorpay.payment_gateway.payment.gateway.adapter;

import com.razorpay.payment_gateway.payment.gateway.PaymentAdapter;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;
import jakarta.persistence.Id;

public class NetBankingAdapter implements PaymentAdapter {

    @Override
    public void initiate(PaymentRequest request) {

    }
}
