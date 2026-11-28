package com.razorpay.payment_gateway.payment.entity;

import com.razorpay.payment_gateway.common.entity.BaseEntity;
import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.PaymentMethod;
import com.razorpay.payment_gateway.common.enums.PaymentStatus;
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
import java.util.UUID;

/**
 * One attempt to pay an {@link OrderRecord}. An order can have many payments
 * (e.g. failed attempts followed by a successful one).
 *
 * <p>Lifecycle: CREATED -> AUTHORIZED -> CAPTURED -> PARTIALLY_REFUNDED / REFUNDED,
 * or CREATED / AUTHORIZED -> FAILED. Transitions are enforced in the service
 * layer using atomic conditional UPDATEs; the DB enforces that each status has
 * its required fields (see {@code chk_payment_*} constraints).
 *
 * <p><b>PCI:</b> never store PAN, CVV, expiry or UPI PIN anywhere in this
 * entity. {@code methodDetails} holds only safe descriptors (token, last4,
 * network, masked VPA, bank code).
 */
@Entity
@Table(
        name = "payment",
        indexes = {
                @Index(name = "idx_payment_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_merchant_id", columnList = "merchant_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"order", "methodDetails"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** The order being paid. Immutable. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    OrderRecord order;

    /** Denormalized from the order for fast tenant-scoped queries. The DB guarantees it matches the order's merchant. */
    @NotNull
    @Column(name = "merchant_id", nullable = false, updatable = false)
    UUID merchantId;

    /** Amount in minor units + currency. Must equal the order amount (service-layer rule). Immutable. */
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

    /** Total refunded so far in minor units. */
    @Min(0)
    @Column(name = "amount_refunded", nullable = false)
    @Builder.Default
    long amountRefunded = 0L;

    /** Client-supplied key making payment creation safe to retry. Unique per merchant. */
    @NotBlank
    @Size(max = 64)
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
    String idempotencyKey;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 25)
    @Builder.Default
    PaymentStatus paymentStatus = PaymentStatus.CREATED;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    PaymentMethod method;

    /** Safe descriptors only (token, last4, network, masked VPA, bank code). Never raw card or PIN data. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "method_details", columnDefinition = "jsonb")
    Map<String, Object> methodDetails;

    /** Reference returned by the bank / acquirer (RRN, ARN, UPI txn id). */
    @Size(max = 64)
    @Column(name = "bank_reference", length = 64)
    String bankReference;

    @Size(max = 64)
    @Column(name = "processor_reference", length = 64)
    String processorReference;

    @Size(max = 50)
    @Column(name = "error_code", length = 50)
    String errorCode;

    @Size(max = 500)
    @Column(name = "error_description", length = 500)
    String errorDescription;

    @Column(name = "authorized_at")
    Instant authorizedAt;

    @Column(name = "captured_at")
    Instant capturedAt;

    @Column(name = "failed_at")
    Instant failedAt;

    /** Set when the payment becomes fully refunded. */
    @Column(name = "refunded_at")
    Instant refundedAt;


    @AssertTrue(message = "amountRefunded cannot exceed the payment amount")
    private boolean isRefundWithinAmount() {
        return amount == null || amountRefunded <= amount.getAmount();
    }

    @AssertTrue(message = "FAILED payments require errorCode and failedAt")
    private boolean isFailureDetailsPresent() {
        return paymentStatus != PaymentStatus.FAILED || (errorCode != null && failedAt != null);
    }

    @PrePersist
    @PreUpdate
    void normalize() {
        if (idempotencyKey != null) idempotencyKey = idempotencyKey.trim();
        if (bankReference != null && bankReference.isBlank()) bankReference = null;
    }
}