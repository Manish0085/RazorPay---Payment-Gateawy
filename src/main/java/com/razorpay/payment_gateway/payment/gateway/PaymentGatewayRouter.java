package com.razorpay.payment_gateway.payment.gateway;

import com.razorpay.payment_gateway.common.enums.PaymentMethod;
import com.razorpay.payment_gateway.payment.gateway.dto.PaymentRequest;
import com.razorpay.payment_gateway.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;


@RequiredArgsConstructor
@Service
public class PaymentGatewayRouter {


    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;
    public void initiate(PaymentRequest request) {
        PaymentAdapter adapter = paymentAdapters.get(request.method());
        if (adapter == null) {
            throw new IllegalArgumentException("No payment adapter register for method: " + request.method());
        }
        adapter.initiate(request);

    }
}
