package com.razorpay.payment_gateway.payment.service;

import com.razorpay.payment_gateway.payment.dto.request.CreateOrderRequest;
import com.razorpay.payment_gateway.payment.dto.response.OrderResponse;
import com.razorpay.payment_gateway.payment.dto.response.PaymentResponse;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse create(UUID merchantId, CreateOrderRequest request);

    OrderResponse getByID(UUID merchantId, UUID orderId);

    OrderResponse cancel(UUID merchantId, UUID orderId);

    List<PaymentResponse> listPayments(UUID merchantId, UUID orderId);
}
