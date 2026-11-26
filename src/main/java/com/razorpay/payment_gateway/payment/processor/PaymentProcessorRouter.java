package com.razorpay.payment_gateway.payment.processor;

import com.razorpay.payment_gateway.common.enums.PaymentMethod;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorResponse;

import java.util.Map;

public class PaymentProcessorRouter {

    private Map<PaymentMethod, PaymentProcessor> paymentProcessors;
    public PaymentProcessorResponse charge(PaymentProcessorRequest request){
        PaymentProcessor processor = paymentProcessors.get(request.method());
        if (processor == null) {
            throw new IllegalArgumentException("No payment processor found for the method " + request.method());
        }
        return processor.charge(request);
    }
}
