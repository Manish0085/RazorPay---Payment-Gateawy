package com.razorpay.payment_gateway.payment.entity;

import com.razorpay.payment_gateway.common.enums.PaymentActor;
import com.razorpay.payment_gateway.common.enums.PaymentEvent;
import com.razorpay.payment_gateway.common.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only record of one status change of a {@link Payment}.
 *
 * <p>Written in the same transaction as the status update. Rows are never
 * updated or deleted (enforced by {@code @Immutable} and a DB trigger).
 * {@code fromStatus} is null only for the creation event.
 *
 * <p>{@code sourceEventId} identifies the external trigger (bank webhook id,
 * acquirer callback id) so replayed events are logged once.
 */
@Entity
@Immutable
@Table(name = "payment_transition_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "payment")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransitionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_ptl_payment"))
    Payment payment;

    /** Status before the change. Null only for the creation event. */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", updatable = false, length = 25)
    PaymentStatus fromStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, updatable = false, length = 25)
    PaymentStatus toStatus;

    /** What caused the change. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false, updatable = false, length = 30)
    PaymentEvent event;

    /** Who or what triggered it, e.g. api:pg_live_xxx, webhook:hdfc, system:expiry-job, admin:{uuid}. */
    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(name = "actor", nullable = false, updatable = false)
    PaymentActor actor;

//    /** External event/callback id used to deduplicate replays. Optional. */
//    @Size(max = 100)
//    @Column(name = "source_event_id", updatable = false, length = 100)
//    String sourceEventId;

    /** Short human-readable note or error code. No PII or card data. */
    @Size(max = 255)
    @Column(name = "reason", updatable = false, length = 255)
    String reason;

    /** When the change happened (may come from the bank). */
    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    Instant occurredAt;


    @AssertTrue(message = "fromStatus and toStatus must differ")
    private boolean isActualChange() {
        return fromStatus == null || fromStatus != toStatus;
    }

//    @PrePersist
//    void normalize() {
//        if (sourceEventId != null && sourceEventId.isBlank()) sourceEventId = null;
//        if (reason != null && reason.isBlank()) reason = null;
//        if (occurredAt == null) occurredAt = Instant.now();
//    }
}