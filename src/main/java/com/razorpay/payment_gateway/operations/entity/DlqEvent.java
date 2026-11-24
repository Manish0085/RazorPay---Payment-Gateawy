package com.razorpay.payment_gateway.operations.entity;

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
@Table(name = "dlq_event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DlqEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "merchant_id", nullable = false)
    UUID merchantId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_event")
    WebhookEvent webhookEvent;
    @Column(length = 1000)
    String finalError;

    @JdbcTypeCode((SqlTypes.JSON))
    @Column(nullable = false, columnDefinition = "json")
    Map<String, Object> payload;

    Instant movedAt;
    Instant replayedAt;

}
