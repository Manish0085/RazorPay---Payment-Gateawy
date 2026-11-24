package com.razorpay.payment_gateway.merchant.controller;

import com.razorpay.payment_gateway.merchant.dto.request.CreateApiKeyRequest;
import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.payment_gateway.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/{merchantId}/api-key")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiKeyController {

    ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(@PathVariable UUID merchantId,
                                                       @Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.create(merchantId, request))
    }
}
