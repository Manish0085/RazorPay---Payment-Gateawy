package com.razorpay.payment_gateway.payment.processor.dto;

import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.PaymentMethod;

import java.util.Map;

public sealed interface PaymentProcessorResponse permits
        PaymentProcessorResponse.Pending,
        PaymentProcessorResponse.Failure,
        PaymentProcessorResponse.Success{

    record Pending(String processorReference, String bankReference) implements PaymentProcessorResponse{}
    record Success(String processorReference, String bankReference) implements PaymentProcessorResponse{}
    record Failure(String errorCode, String errorDescription) implements PaymentProcessorResponse{}
}
