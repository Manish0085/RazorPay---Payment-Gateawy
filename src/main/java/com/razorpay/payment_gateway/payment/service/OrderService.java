package com.razorpay.payment_gateway.payment.service;

import com.razorpay.payment_gateway.payment.dto.request.CreateOrderRequest;
import com.razorpay.payment_gateway.payment.dto.response.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface OrderService {
    OrderResponse create(UUID merchantId, CreateOrderRequest request);
}
