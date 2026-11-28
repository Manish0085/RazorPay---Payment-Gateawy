package com.razorpay.payment_gateway.merchant.entity;

import com.razorpay.payment_gateway.common.entity.BaseEntity;
import com.razorpay.payment_gateway.common.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * A dashboard login belonging to a merchant (or a platform-level admin).
 *
 * <p>Merchant-scoped roles must reference a {@link Merchant}; platform roles
 * (e.g. ADMIN, SUPPORT) have a null merchant. This is enforced by the
 * {@code chk_app_user_role_merchant} DB constraint.
 *
 * <p><b>Security:</b> {@code passwordHash} holds a BCrypt/Argon2 hash only.
 * Never expose this entity from a controller; map it to a response DTO.
 */
@Entity
@Table(
        name = "app_user",
        indexes = {
                @Index(name = "idx_app_user_merchant_id", columnList = "merchant_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"passwordHash", "merchant"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppUser extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Owning merchant. Null only for platform-level users. */
    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "merchant_id", foreignKey = @ForeignKey(name = "fk_app_user_merchant"))
    @JoinColumn(name = "merchant_id")
    Merchant merchant;

    /** Login email. Stored lowercase, unique across all users. */
    @NotBlank
    @Email
    @Size(max = 254)
    @Column(nullable = false, unique = true, length = 254)
    String email;

    /** Password hash (BCrypt is 60 chars). The raw password is validated in the request DTO, never here. */
    @NotBlank
    @Size(max = 100)
    @Column(name = "password_hash", nullable = false, length = 100)
    String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    UserRole role;


    @PrePersist
    @PreUpdate
    void normalize() {
        if (email != null) email = email.trim().toLowerCase();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );
    }

    @Override
    public @Nullable String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}