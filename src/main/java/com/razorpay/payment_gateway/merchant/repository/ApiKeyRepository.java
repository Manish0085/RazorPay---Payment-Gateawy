package com.razorpay.payment_gateway.merchant.repository;

import com.razorpay.payment_gateway.merchant.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
}
