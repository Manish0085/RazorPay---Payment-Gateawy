package com.razorpay.payment_gateway.vault.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

/**
 * A stored card, envelope-encrypted (AES-256-GCM).
 *
 * <p>{@code encryptedPan} = IV (12 bytes) || ciphertext || GCM tag, encrypted with a
 * per-card data key (DEK). {@code encryptedDek} is that DEK wrapped by a KMS-managed
 * key (KEK), identified by {@code kekKeyId} so keys can be rotated. Use {@code token}
 * as GCM associated data so a ciphertext can't be swapped between rows.
 *
 * <p><b>Never store the CVV/CVC</b>, even encrypted. It must not exist in this table or
 * any log.
 *
 * <p>Deletion is a crypto-shred: set {@code deletedAt} and null the PAN, DEK, KEK id and
 * fingerprint. The row stays so existing payments keep their last4/brand. This is
 * enforced by {@code chk_vault_card_secret_lifecycle}.
 */
@Entity
@Table(name = "vault_card")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"encryptedPan", "encryptedDek", "fingerprint", "cardHolderName"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VaultCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

//    /** Owning merchant. Immutable. Kept as a plain id so the vault module stays isolated. */
//    @NotNull
//    @Column(name = "merchant_id", nullable = false, updatable = false)
//    UUID merchantId;
//
//    /** Owning customer. Null for one-time (guest) cards. Immutable. */
//    @Column(name = "customer_id", updatable = false)
//    UUID customerId;

    /** Public reference handed to merchants instead of card data, e.g. tok_AbC123.... */
    @NotBlank
    @Pattern(regexp = "^tok_[A-Za-z0-9]{20,32}$")
    @Column(nullable = false, updatable = false, unique = true, length = 40)
    String token;

    /**
     * HMAC-SHA256 (hex) of the PAN with a dedicated secret key, used to detect the same
     * card saved twice. Never a plain hash: PANs are low-entropy and easy to brute-force.
     */
    @Pattern(regexp = "^[0-9a-f]{64}$")
    @Column(length = 64)
    String fingerprint;

    /** Last four digits, safe to display. */
    @NotBlank
    @Pattern(regexp = "^[0-9]{4}$")
    @Column(name = "last_four", nullable = false, updatable = false, length = 4)
    String lastFour;

    /** First 6 or 8 digits (issuer identification number). */
    @NotBlank
    @Pattern(regexp = "^([0-9]{6}|[0-9]{8})$")
    @Column(nullable = false, updatable = false, length = 8)
    String bin;

    /** IV || ciphertext || tag. Null only after deletion. */
    @Column(name = "encrypted_pan", columnDefinition = "bytea")
    byte[] encryptedPan;

    /** DEK wrapped by the KMS key. Null only after deletion. */
    @Column(name = "encrypted_dek", columnDefinition = "bytea")
    byte[] encryptedDek;

    /** KMS key id/version that wrapped the DEK; needed for decryption and rotation. */
    @Size(max = 200)
    @Column(name = "kek_key_id", length = 200)
    String kekKeyId;

    @NotNull
    @Column(nullable = false, updatable = false, length = 20)
    String brand;

    /** 1-12. */
    @NotNull
    @Min(1) @Max(12)
    @Column(name = "expiry_month", nullable = false)
    Integer expiryMonth;

    /** Four-digit year. */
    @NotNull
    @Min(2000) @Max(2099)
    @Column(name = "expiry_year", nullable = false)
    Integer expiryYear;

    /** Name printed on the card (PII). */
    @Size(max = 100)
    @Column(name = "card_holder_name", length = 100)
    String cardHolderName;

    /** Soft-delete marker. Set together with nulling all secret columns. */
    @Column(name = "deleted_at")
    Instant deletedAt;

    @AssertTrue(message = "Live cards need PAN, DEK, KEK id and fingerprint; deleted cards must have none")
    private boolean isSecretLifecycleConsistent() {
        boolean hasAll = encryptedPan != null && encryptedPan.length > 0
                && encryptedDek != null && encryptedDek.length > 0
                && kekKeyId != null && !kekKeyId.isBlank()
                && fingerprint != null;
        boolean hasNone = encryptedPan == null && encryptedDek == null
                && kekKeyId == null && fingerprint == null;
        return deletedAt == null ? hasAll : hasNone;
    }

    @PrePersist
    @PreUpdate
    void normalize() {
        if (cardHolderName != null) {
            cardHolderName = cardHolderName.trim();
            if (cardHolderName.isEmpty()) cardHolderName = null;
        }
    }
}