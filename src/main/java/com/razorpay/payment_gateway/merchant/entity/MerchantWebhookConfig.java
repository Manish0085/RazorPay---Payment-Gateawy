package com.razorpay.payment_gateway.merchant.entity;

import com.razorpay.payment_gateway.common.entity.BaseEntity;
import com.razorpay.payment_gateway.common.enums.Environment;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A merchant's webhook endpoint and the events it is subscribed to.
 *
 * <p>Each delivery is signed with HMAC-SHA256 using the endpoint's secret, so the
 * secret is stored <b>encrypted</b> (recoverable), never hashed. It is shown to the
 * merchant once at creation or rotation.
 *
 * <p>LIVE endpoints must use HTTPS. SSRF checks (no private/loopback/link-local
 * addresses, re-resolved at send time) are done in the service layer.
 */
@Entity
@Table(
        name = "merchant_webhook_config",
        indexes = {
                @Index(name = "idx_webhook_merchant_id", columnList = "merchant_id, enabled")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"merchant", "webhookSecretEncrypted"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MerchantWebhookConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Owning merchant. Immutable. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false, updatable = false)
    Merchant merchant;

    /** Which environment's events are sent here. Immutable. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    Environment environment;

    /** Endpoint that receives POST deliveries. */
    @NotBlank
    @Size(max = 500)
    @Column(name = "target_url", nullable = false, length = 500)
    String targetUrl;

    /** AES-GCM ciphertext (via AttributeConverter) of the signing secret. Never log or return it. */
    @NotBlank
    @Size(max = 255)
    @Column(name = "webhook_secret_encrypted", nullable = false, length = 255)
    String webhookSecretHash;

    /** Subscribed event names, e.g. payment.captured. At least one, at most 20. */
    @NotEmpty
    @Size(max = 20)
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "events", nullable = false, columnDefinition = "text[]")
//    List<@NotBlank @Pattern(regexp = "^[a-z_]+\\.[a-z_]+$") String> events;
    String eventType;

    @Column(nullable = false)
    @Builder.Default
    boolean enabled = true;


}