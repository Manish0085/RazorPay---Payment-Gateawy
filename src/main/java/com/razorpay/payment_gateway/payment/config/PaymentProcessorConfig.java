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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    CardPaymentProcessor cardPaymentProcessor;
    NetBankingPaymentProcessor netBankingPaymentProcessor;
    UpiPaymentProcessor upiPaymentProcessor;


    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentAdapterMap() {
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor,
                PaymentMethod.NETBANKING, netBankingPaymentProcessor
        );
    }
}
