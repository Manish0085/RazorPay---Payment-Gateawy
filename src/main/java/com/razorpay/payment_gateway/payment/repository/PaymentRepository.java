package com.razorpay.payment_gateway.payment.repository;

import com.razorpay.payment_gateway.common.enums.PaymentStatus;
import com.razorpay.payment_gateway.payment.entity.OrderRecord;
import com.razorpay.payment_gateway.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByOrder_Id(OrderRecord order);

    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus authorizing, Instant globalWindow);
}
