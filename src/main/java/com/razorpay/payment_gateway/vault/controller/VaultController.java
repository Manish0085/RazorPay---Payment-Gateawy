package com.razorpay.payment_gateway.vault.controller;

import com.razorpay.payment_gateway.vault.dto.request.TokenizeRequest;
import com.razorpay.payment_gateway.vault.dto.response.TokenizeResponse;
import com.razorpay.payment_gateway.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
@Validated
public class VaultController {

    private final VaultService vaultService;
    UUID merchantId = UUID.randomUUID();
    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(
            @RequestBody @Valid TokenizeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaultService.tokenize(request, merchantId));
    }
}
