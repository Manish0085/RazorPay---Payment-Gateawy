package com.razorpay.payment_gateway.payment.repository;

import com.razorpay.payment_gateway.payment.entity.OrderRecord;
import com.razorpay.payment_gateway.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByOrder_Id(OrderRecord order);
}
