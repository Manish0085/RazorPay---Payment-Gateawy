package com.razorpay.payment_gateway.payment.entity;

import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.RefundStatus;
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
 * A refund (full or partial) against a captured {@link Payment}.
 *
 * <p>Lifecycle: PENDING -> PROCESSED or FAILED (both terminal). The refund amount is
 * reserved on the payment's {@code amountRefunded} when the refund is created and
 * released if it FAILS. That update is a conditional UPDATE in the same transaction
 * (see notes), so total refunds can never exceed the payment amount.
 *
 * <p>Amount and currency must equal the payment's currency (enforced by a composite FK).
 */
@Entity
@Table(name = "refund")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"payment", "notes"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Payment being refunded. Immutable. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    Payment payment;

    /** Denormalized from the payment; the DB guarantees it matches. */
    @NotNull
    @Column(name = "merchant_id", nullable = false, updatable = false)
    UUID merchantId;

    /** Refund amount in minor units + currency. Immutable. */
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

    /** Makes refund creation safe to retry. Unique per merchant. */
    @NotBlank
    @Size(max = 64)
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
    String idempotencyKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    RefundStatus status = RefundStatus.PENDING;

    /** Merchant-visible reason, e.g. "customer cancelled". No PII. */
    @Size(max = 255)
    @Column(length = 255, updatable = false)
    String reason;

    /** Reference from the bank / acquirer (ARN, RRN). */
    @Size(max = 64)
    @Column(name = "bank_reference", length = 64)
    String bankReference;

    @Size(max = 50)
    @Column(name = "error_code", length = 50)
    String errorCode;

    @Size(max = 500)
    @Column(name = "error_description", length = 500)
    String errorDescription;

    /** Free-form merchant metadata. Max 15 keys. */
    @Size(max = 15)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Map<String, String> notes;

    /** When the refund reached a terminal state (PROCESSED or FAILED). Null while PENDING. */
    @Column(name = "processed_at")
    Instant processedAt;



    @AssertTrue(message = "FAILED refunds require errorCode")
    private boolean isFailureDetailsPresent() {
        return status != RefundStatus.FAILED || (errorCode != null && !errorCode.isBlank());
    }

    @AssertTrue(message = "PENDING refunds cannot have processedAt")
    private boolean isPendingConsistent() {
        return status != RefundStatus.PENDING || processedAt == null;
    }

}