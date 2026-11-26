package com.razorpay.payment_gateway.operations.entity;

import com.razorpay.payment_gateway.common.entity.BaseEntity;
import com.razorpay.payment_gateway.common.enums.WebhookEventStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "webhookEvent")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebhookEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    UUID merchantId;

    @Column(nullable = false, length = 100)
    String eventType;

    @JdbcTypeCode(value = SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    Map<String, Object> payload;

    @Column(nullable = false)
    String targetUrl;

    @Column(nullable = false)
    String signature;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    WebhookEventStatus status;

    @Column(nullable = false)
    @Builder.Default
    Integer attempts = 0;
    Instant nextRetryAt;

    Instant lastAttemptAt;

    Integer lastResponseCode;

    @Column(length = 1000)
    String lastResponseBody;
    Instant deliveredAt;

}
