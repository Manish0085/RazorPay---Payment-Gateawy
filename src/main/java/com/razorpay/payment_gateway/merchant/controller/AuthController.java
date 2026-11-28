package com.razorpay.payment_gateway.merchant.controller;

import com.razorpay.payment_gateway.merchant.dto.request.LoginRequest;
import com.razorpay.payment_gateway.merchant.dto.response.LoginResponse;
import com.razorpay.payment_gateway.merchant.dto.response.MerchantResponse;
import com.razorpay.payment_gateway.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.payment_gateway.merchant.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/v1/auth")
public class AuthController {

    AuthService authService;
    @PostMapping("/signup")
    public ResponseEntity<MerchantResponse> signup(@RequestBody @Valid MerchantSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.login(request));
    }

}
