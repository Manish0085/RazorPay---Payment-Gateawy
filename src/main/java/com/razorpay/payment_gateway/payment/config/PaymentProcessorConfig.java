package com.razorpay.payment_gateway.payment.config;

import com.razorpay.payment_gateway.common.enums.PaymentMethod;
import com.razorpay.payment_gateway.payment.gateway.PaymentAdapter;
import com.razorpay.payment_gateway.payment.gateway.adapter.CardPaymentAdapter;
import com.razorpay.payment_gateway.payment.gateway.adapter.NetBankingAdapter;
import com.razorpay.payment_gateway.payment.gateway.adapter.UpiPaymentAdapter;
import com.razorpay.payment_gateway.payment.processor.PaymentProcessor;
import com.razorpay.payment_gateway.payment.processor.strategy.CardPaymentProcessor;
import com.razorpay.payment_gateway.payment.processor.strategy.NetBankingPaymentProcessor;
import com.razorpay.payment_gateway.payment.processor.strategy.UpiPaymentProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentProcessorConfig {

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentAdapterMap() {
        return Map.of(
                PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor(),
                PaymentMethod.NETBANKING, new NetBankingPaymentProcessor()
        );
    }
}
