package com.razorpay.payment_gateway.vault.service.impl;

import com.razorpay.payment_gateway.vault.dto.request.TokenizeRequest;
import com.razorpay.payment_gateway.vault.dto.response.TokenizeResponse;
import com.razorpay.payment_gateway.vault.repository.CardTokenRepository;
import com.razorpay.payment_gateway.vault.repository.VaultCardRepository;
import com.razorpay.payment_gateway.vault.service.VaultService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VaultServiceImpl implements VaultService {

    CardTokenRepository cardTokenRepository;
    VaultCardRepository vaultCardRepository;

    @Override
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {

        return null;
    }
}
