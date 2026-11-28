package com.razorpay.payment_gateway.payment.controller;

import com.razorpay.payment_gateway.merchant.security.MerchantContext;
import com.razorpay.payment_gateway.payment.dto.request.PaymentInitRequest;
import com.razorpay.payment_gateway.payment.dto.response.PaymentResponse;
import com.razorpay.payment_gateway.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Validated
@RequiredArgsConstructor
public class PaymentController {

    PaymentService paymentService;
    MerchantContext merchantContext;


    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestBody @Valid PaymentInitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiate(merchantContext.getMerchantId(), request));
    }


    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }
}
