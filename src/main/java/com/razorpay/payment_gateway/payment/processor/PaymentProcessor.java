package com.razorpay.payment_gateway.payment.processor;

import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
