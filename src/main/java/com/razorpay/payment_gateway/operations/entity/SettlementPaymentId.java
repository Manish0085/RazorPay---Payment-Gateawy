package com.razorpay.payment_gateway.operations.entity;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class SettlementPaymentId {

    UUID settlementId;
    UUID paymentId;
}
