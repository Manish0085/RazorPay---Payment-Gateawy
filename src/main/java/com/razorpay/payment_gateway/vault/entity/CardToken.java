package com.razorpay.payment_gateway.vault.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "card_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"vaultCard"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Owning merchant. Immutable. Kept as a plain id so the vault module stays isolated. */
    @NotNull
    @Column(name = "merchant_id", nullable = false, updatable = false)
    UUID merchantId;

    @Column(nullable = false, unique = true, length = 50)
    String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vault_card_id", nullable = false)
    VaultCard vaultCard;

    @NotNull
    @Column(nullable = false)
    UUID customer;

    Instant revokeAt;
}
