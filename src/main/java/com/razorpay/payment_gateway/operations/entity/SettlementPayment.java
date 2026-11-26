package com.razorpay.payment_gateway.operations.entity;

import com.razorpay.payment_gateway.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "settlement_payment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SettlementPayment extends BaseEntity {

    @EmbeddedId
    SettlementPaymentId id;

    @MapsId(value = "settlementId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    Settlement settlement;

}
