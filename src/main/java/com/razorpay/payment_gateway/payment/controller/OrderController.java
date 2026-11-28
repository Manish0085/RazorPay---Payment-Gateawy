package com.razorpay.payment_gateway.payment.controller;

import com.razorpay.payment_gateway.merchant.security.MerchantContext;
import com.razorpay.payment_gateway.payment.dto.request.CreateOrderRequest;
import com.razorpay.payment_gateway.payment.dto.response.OrderResponse;
import com.razorpay.payment_gateway.payment.service.OrderService;
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

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {
    OrderService orderService;
    MerchantContext merchantContext;
//    UUID merchantID = UUID.fromString(""); // TODO: replace it with MerchantContext
    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(merchantContext.getMerchantId(), request));
    }

}
