package com.razorpay.payment_gateway.payment.simulator;

import com.razorpay.payment_gateway.common.enums.PaymentStatus;
import com.razorpay.payment_gateway.payment.entity.Payment;
import com.razorpay.payment_gateway.payment.repository.PaymentRepository;
import com.razorpay.payment_gateway.payment.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BankCallbackSimulator {

    PaymentRepository paymentRepository;
    PaymentService paymentService;
    SimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallbacks() {
        Instant globalWindow = Instant.now().minusSeconds(1);

        List<Payment> candidates =
                paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);

        if (candidates.isEmpty()) return;

        for (Payment payment: candidates) {
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {



    }
}
