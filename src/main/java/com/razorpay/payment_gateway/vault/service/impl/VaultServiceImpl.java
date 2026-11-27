package com.razorpay.payment_gateway.vault.service.impl;

import com.razorpay.payment_gateway.common.entity.Money;
import com.razorpay.payment_gateway.common.enums.CardBrand;
import com.razorpay.payment_gateway.common.exception.ResourceNotFoundException;
import com.razorpay.payment_gateway.common.util.RandomizerUtil;
import com.razorpay.payment_gateway.payment.processor.PaymentProcessorRouter;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorResponse;
import com.razorpay.payment_gateway.vault.config.VaultEncryptionConfig;
import com.razorpay.payment_gateway.vault.dto.request.TokenizeRequest;
import com.razorpay.payment_gateway.vault.dto.response.TokenizeResponse;
import com.razorpay.payment_gateway.vault.entity.CardToken;
import com.razorpay.payment_gateway.vault.entity.VaultCard;
import com.razorpay.payment_gateway.vault.repository.CardTokenRepository;
import com.razorpay.payment_gateway.vault.repository.VaultCardRepository;
import com.razorpay.payment_gateway.vault.service.VaultService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VaultServiceImpl implements VaultService {

    CardTokenRepository cardTokenRepository;
    VaultCardRepository vaultCardRepository;
    BytesEncryptor dekEncrypter;
    PaymentProcessorRouter paymentProcessorRouter;
    
    
    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {
        String lastFour = request.pan().substring(request.pan().length()-4);
        String bin = request.pan().substring(0, 6);
        CardBrand cardBrand = detectBrand(request.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncryptor(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));

        byte[] encryptDek = dekEncrypter.encrypt(dek);

        VaultCard vaultCard = VaultCard.builder()
                .cardHolderName(request.cardHolderName())
                .bin(bin)
                .brand(cardBrand)
                .expiryMonth(request.expiryMonth())
                .expiryYear(request.expiryYear())
                .lastFour(lastFour)
                .encryptedDek(encryptDek)
                .encryptedPan(encryptedPan)
                .build();

        vaultCard = vaultCardRepository.save(vaultCard);
        String token = "tok_" + RandomizerUtil.randomBase64(32);


        CardToken cardToken = CardToken.builder()
                .vaultCard(vaultCard)
                .customer(request.customerId())
                .merchantId(merchantId)
                .token(token)
                .build();

        return new TokenizeResponse(token, lastFour, cardBrand,
                request.expiryMonth(), request.expiryYear());
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {

        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("CARD_TOKEN_NOT_FOUND", "Card with Token " + token + " not found"));

        VaultCard vaultCard = cardToken.getVaultCard();
        byte[] panBytes = null;

        try {
            byte[] dek = dekEncrypter.decrypt(vaultCard.getEncryptedDek());
            panBytes = VaultEncryptionConfig.panEncryptor(dek).decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest
                    .card(paymentId, pan, expiry, amount, methodDetails);

            PaymentProcessorResponse response = paymentProcessorRouter.charge(paymentProcessorRequest);
            log.info("Vault charge registered, token: {}**** ", token.substring(0, 4));


            return response;
        } catch (Exception e) {
            log.warn("Vault charge couldn't register, token: {}**** ", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", "Vault change failed");
        } finally {
            if (panBytes != null)
                Arrays.fill(panBytes, (byte) 0);
        }
    }

    private CardBrand detectBrand(String pan) {

        if (pan.startsWith("4")) return CardBrand.VISA;
        if (pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if (pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
