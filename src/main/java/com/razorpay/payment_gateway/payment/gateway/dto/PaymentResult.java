package com.razorpay.payment_gateway.payment.gateway.dto;

public sealed interface PaymentResult permits PaymentResult.Failure, PaymentResult.Pending{

    record Pending(String registrationRef) implements PaymentResult{}
    record Failure(String errorCode, String description) implements PaymentResult{}
}
