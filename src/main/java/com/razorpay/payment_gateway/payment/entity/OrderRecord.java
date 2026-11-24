package com.razorpay.payment_gateway.payment.entity;

import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A request by a merchant to collect a fixed amount from a customer.
 * Payments are attempted against an order.
 *
 * <p>Amount and currency are immutable after creation. An order moves
 * CREATED -> ATTEMPTED -> PAID, or to EXPIRED / CANCELLED. Transitions are
 * validated in the service layer; the DB only restricts the allowed values.
 */
@Entity
@Table(name = "order_record")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Owning merchant. Immutable. FK is enforced in the DB. */
    @NotNull
    @Column(name = "merchant_id", nullable = false, updatable = false)
    UUID merchantId;

    /** Order amount in minor units plus ISO-4217 currency. Immutable. */
    @Valid
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "amount", nullable = false, updatable = false)),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "currency", nullable = false, updatable = false, length = 3))
    })
    Money amount;

    /** Merchant's own reference (e.g. their invoice number). Unique per merchant when present. */
    @Size(max = 40)
    @Column(length = 40, updatable = false)
    String receipt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    @Builder.Default
    OrderStatus orderStatus = OrderStatus.CREATED;

    /** Number of payment attempts so far. Increment atomically with an UPDATE query. */
    @Min(0)
    @Column(nullable = false)
    @Builder.Default
    int attempts = 0;

    /** Free-form merchant metadata. Max 15 keys; keys up to 64 chars, values up to 256. */
    @Size(max = 15)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Map<String, Object> notes;

    /** After this instant the order can no longer be paid. */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;


    /** Single place to decide whether a payment may be started now. */
    public boolean isPayableAt(Instant now) {
        return (orderStatus == OrderStatus.CREATED || orderStatus == OrderStatus.ATTEMPTED)
                && now.isBefore(expiresAt);
    }
}