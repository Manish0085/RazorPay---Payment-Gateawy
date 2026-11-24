package com.razorpay.payment_gateway.merchant.repository;

import com.razorpay.payment_gateway.merchant.entity.AppUser;
import com.razorpay.payment_gateway.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
}
