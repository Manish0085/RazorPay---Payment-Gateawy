package com.razorpay.payment_gateway.merchant.entity;

import com.razorpay.payment_gateway.common.enums.Environment;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * An API credential pair for a merchant. Each row is one key pair in one
 * {@link Environment} (TEST or LIVE).
 *
 * <p>{@code keyId} is public (e.g. {@code pg_live_AbC123xyz...}) and sent as the
 * username in Basic auth. The secret is shown once at creation; only its
 * SHA-256 hex digest is stored in {@code keySecretHash}.
 *
 * <p><b>Rotation:</b> rotating creates a NEW row. The old row gets
 * {@code rotatedAt} and {@code gracePeriodExpiresAt}; it stays valid until
 * that time, then must be rejected. See {@link #isUsableAt(Instant)}.
 */
@Entity
@Table(name = "api_key")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"merchant", "keySecretHash"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Owning merchant. Immutable after creation. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false, updatable = false)
    Merchant merchant;

    /** Public key identifier, globally unique. Prefix must match the environment. */
    @NotBlank
    @Pattern(regexp = "^pg_(test|live)_[A-Za-z0-9]{14,32}$")
    @Column(name = "key_id", nullable = false, updatable = false, unique = true, length = 50)
    String keyId;

    /** Lowercase hex SHA-256 of the secret (64 chars). Never the raw secret. */
    @NotBlank
    @Pattern(regexp = "^[0-9a-f]{64}$")
    @Column(name = "key_secret_hash", nullable = false, length = 100)
    String keySecretHash;

    @NotBlank
    @Pattern(regexp = "^[0-9a-f]{64}$")
    @Column(name = "key_secret_hash", length = 100)
    String previousKeySecretHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    Environment environment;

    /** Set to false to revoke the key immediately. */
    @Column(nullable = false)
    @Builder.Default
    boolean enabled = true;

    /** Last successful authentication (updated by a throttled query, not entity save). */
    @Column(name = "last_used_at")
    Instant lastUsedAt;

    /** When this key was replaced by a newer one. Null if never rotated. */
    @Column(name = "rotated_at")
    Instant rotatedAt;

    /** After this instant a rotated key stops working. Requires rotatedAt. */
    @Column(name = "grace_period_expires_at")
    Instant gracePeriodExpiresAt;



//    @AssertTrue(message = "gracePeriodExpiresAt requires rotatedAt and must be after it")
//    private boolean isGracePeriodValid() {
//        if (gracePeriodExpiresAt == null) return true;
//        return rotatedAt != null && gracePeriodExpiresAt.isAfter(rotatedAt);
//    }
//
//    /** Single place to decide whether this key may authenticate a request right now. */
//    public boolean isUsableAt(Instant now) {
//        if (!enabled) return false;
//        if (rotatedAt == null) return true;
//        return gracePeriodExpiresAt != null && now.isBefore(gracePeriodExpiresAt);
//    }
}