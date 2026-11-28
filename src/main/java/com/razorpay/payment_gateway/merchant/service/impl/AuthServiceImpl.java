package com.razorpay.payment_gateway.merchant.service.impl;

import com.razorpay.payment_gateway.common.enums.MerchantStatus;
import com.razorpay.payment_gateway.common.enums.UserRole;
import com.razorpay.payment_gateway.common.exception.DuplicateResourceException;
import com.razorpay.payment_gateway.common.exception.ResourceNotFoundException;
import com.razorpay.payment_gateway.merchant.dto.request.LoginRequest;
import com.razorpay.payment_gateway.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.payment_gateway.merchant.dto.response.LoginResponse;
import com.razorpay.payment_gateway.merchant.dto.response.MerchantResponse;
import com.razorpay.payment_gateway.merchant.entity.AppUser;
import com.razorpay.payment_gateway.merchant.entity.Merchant;
import com.razorpay.payment_gateway.merchant.mapper.MerchantMapper;
import com.razorpay.payment_gateway.merchant.repository.AppUserRepository;
import com.razorpay.payment_gateway.merchant.repository.MerchantRepository;
import com.razorpay.payment_gateway.merchant.security.JwtUtil;
import com.razorpay.payment_gateway.merchant.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    AppUserRepository appUserRepository;
    MerchantRepository merchantRepository;

    MerchantMapper merchantMapper;

    AuthenticationManager authenticationManager;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if (merchantRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL", "Merchant with email " + request.email() + " already exists");
        }

        Merchant merchant = merchantMapper.toEntityFromSignupRequest(request);
        merchant.setMerchantStatus(MerchantStatus.PENDING_KYC);
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .merchant(merchant)
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        AppUser appUser = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("APP_USER_NOT_FOUND", "Invalid credentials"));

        String token = jwtUtil.generateAccessToken(request.email(), appUser.getMerchant().getId(), appUser.getRole().toString());
        return new LoginResponse(token);
}
