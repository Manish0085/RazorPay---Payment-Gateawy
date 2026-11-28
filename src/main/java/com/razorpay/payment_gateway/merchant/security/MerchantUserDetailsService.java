package com.razorpay.payment_gateway.merchant.security;

import com.razorpay.payment_gateway.common.exception.ResourceNotFoundException;
import com.razorpay.payment_gateway.merchant.entity.Merchant;
import com.razorpay.payment_gateway.merchant.repository.AppUserRepository;
import com.razorpay.payment_gateway.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final AppUserRepository appUserRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("APPUSER_NOT_FOUND", "App User with email " + email + " not found"));
    }
}
