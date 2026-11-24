package com.razorpay.payment_gateway.merchant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * An end customer of a merchant (the payer). Scoped to one merchant.
 *
 * <p>At least one of {@code email} or {@code contactNumber} must be present
 * ({@code chk_customer_has_contact}). Email and contact number are unique per
 * merchant among non-deleted customers. Deletion is soft: set {@code deletedAt}.
 */
@Entity
@Table(name = "customer")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "merchant")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Owning merchant. Immutable after creation. */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false, updatable = false)
    Merchant merchant;

    /** Customer's full name. Optional. */
    @Size(max = 200)
    @Column(length = 200)
    String name;

    /** Customer email, stored lowercase. Optional if contact number is given. */
    @Email
    @Size(max = 254)
    @Column(length = 254)
    String email;

    /** Phone number: digits with optional leading '+' (7-15 digits). */
    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    @Column(name = "contact_number", length = 16)
    String contactNumber;

    /** Soft-delete marker. Null means the customer is active. */
    @Column(name = "deleted_at")
    Instant deletedAt;


    @PrePersist
    @PreUpdate
    void normalize() {
        if (name != null) name = name.trim();
        if (email != null) {
            email = email.trim().toLowerCase();
            if (email.isEmpty()) email = null;
        }
        if (contactNumber != null) {
            contactNumber = contactNumber.trim();
            if (contactNumber.isEmpty()) contactNumber = null;
        }
    }
}