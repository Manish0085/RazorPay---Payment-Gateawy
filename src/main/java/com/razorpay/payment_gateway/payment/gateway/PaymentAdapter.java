package com.razorpay.payment_gateway.payment.gateway;

import com.razorpay.payment_gateway.common.enums.PaymentMethod;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentResult;

import java.util.Map;
import java.util.UUID;

public interface PaymentAdapter {

    PaymentResult initiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}
