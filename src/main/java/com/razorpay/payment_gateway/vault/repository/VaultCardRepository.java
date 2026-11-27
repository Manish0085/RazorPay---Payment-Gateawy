package com.razorpay.payment_gateway.vault.repository;

import com.razorpay.payment_gateway.vault.entity.VaultCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VaultCardRepository extends JpaRepository<VaultCard, UUID> {
}
