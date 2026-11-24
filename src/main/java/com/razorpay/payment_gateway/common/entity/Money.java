package com.razorpay.payment_gateway.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Money {

    @Positive
    @Column(nullable = false)
    private long amount;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    @Column(nullable = false, length = 3)
    private String currency;

    private Money(long amountUnits, String currency) {
        this.amount = amountUnits;
        this.currency = currency;
    }

    private static Money of(long amountUnits, String currency) {
        return new Money(amountUnits, currency);
    }

    private static Money inr(long amountUnits) {
        return new Money(amountUnits, "INR");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new RuntimeException("Cannot add money with different currencies");
        return new Money(this.amount + other.amount, this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency))
            throw new RuntimeException("Cannot subtract money with different currencies");
        return new Money(this.amount - other.amount, this.currency);
    }
}
