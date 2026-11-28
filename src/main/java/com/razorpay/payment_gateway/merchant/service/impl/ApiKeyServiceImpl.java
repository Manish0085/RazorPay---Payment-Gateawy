package com.razorpay.payment_gateway.merchant.service.impl;

import com.razorpay.payment_gateway.common.exception.ResourceNotFoundException;
import com.razorpay.payment_gateway.common.util.RandomizerUtil;
import com.razorpay.payment_gateway.merchant.dto.request.CreateApiKeyRequest;
import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyResponse;
import com.razorpay.payment_gateway.merchant.entity.ApiKey;
import com.razorpay.payment_gateway.merchant.entity.Merchant;
import com.razorpay.payment_gateway.merchant.mapper.ApiKeyMapper;
import com.razorpay.payment_gateway.merchant.repository.ApiKeyRepository;
import com.razorpay.payment_gateway.merchant.repository.MerchantRepository;
import com.razorpay.payment_gateway.merchant.service.ApiKeyService;
import com.razorpay.payment_gateway.merchant.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ApiKeyServiceImpl implements ApiKeyService {

    ApiKeyRepository apiKeyRepository;
    MerchantRepository merchantRepository;
    ApiKeyMapper apiKeyMapper;
    BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MERCHANT", merchantId));

        String keyId = "rzp_" + request.environment().name().toLowerCase() + "_" + RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40); // TODO: replace with cryptographic random hex

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(BCRYPT.encode(rawSecret))
                .environment(request.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(apiKey.getId(),
                keyId,
                rawSecret,
                apiKey.getEnvironment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        return apiKeyRepository.findByMerchant_Id(merchantId)
                .stream()
                .map(apiKeyMapper::toResponse
                ).toList();
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(key -> key.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("APIKEY", keyId));

        apiKey.setEnabled(false);
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(key -> key.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("APIKEY", keyId));

        if(!apiKey.isEnabled()) throw new RuntimeException("Cannot rotate a disabled key");
        String newRawSecret = RandomizerUtil.randomBase64(40);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(BCRYPT.encode(newRawSecret));
        apiKey.setRotatedAt(Instant.now());
        apiKey.setGracePeriodExpiresAt(Instant.now().plusSeconds(86400));
        apiKeyRepository.save(apiKey);
        return new ApiKeyCreateResponse(apiKey.getId(),
                apiKey.getKeyId(),
                newRawSecret,
                apiKey.getEnvironment());
    }


}
