package com.razorpay.payment_gateway.merchant.service.impl;

import com.razorpay.payment_gateway.common.enums.MerchantStatus;
import com.razorpay.payment_gateway.common.enums.UserRole;
import com.razorpay.payment_gateway.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.payment_gateway.merchant.dto.response.MerchantResponse;
import com.razorpay.payment_gateway.merchant.entity.AppUser;
import com.razorpay.payment_gateway.merchant.entity.Merchant;
import com.razorpay.payment_gateway.merchant.repository.AppUserRepository;
import com.razorpay.payment_gateway.merchant.repository.MerchantRepository;
import com.razorpay.payment_gateway.merchant.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    AppUserRepository appUserRepository;
    MerchantRepository merchantRepository;

    @Override
    public MerchantResponse signup(MerchantSignupRequest request) {
        if (merchantRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Merchant with email " + request.email() + " already exists");
        }

        Merchant merchant =Merchant.builder()
                .name(request.name())
                .email(request.email())
                .businessName(request.businessName())
                .businessType(request.businessType())
                .merchantStatus(MerchantStatus.PENDING_KYC)
                .build();

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .passwordHash(request.password())
                .role(UserRole.OWNER)
                .merchant(merchant)
                .build();

        appUserRepository.save(appUser);

        return new MerchantResponse(merchant.getId(),
                merchant.getName(),
                merchant.getEmail(),
                merchant.getBusinessName(),
                merchant.getBusinessType(),
                merchant.getMerchantStatus());
    }


}
