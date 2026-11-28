package com.razorpay.payment_gateway.payment.simulator;

import com.razorpay.payment_gateway.common.enums.ChaosMode;
import com.razorpay.payment_gateway.common.enums.PaymentStatus;
import com.razorpay.payment_gateway.common.util.RandomizerUtil;
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

        log.info("Simulating payment for {} payments", candidates.size());
        if (candidates.isEmpty()) return;

        for (Payment payment: candidates) {
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {

        SimulatorConfig.MethodSimulatorConfig methodConfig = simulatorConfig.configFor(payment.getMethod());

        Instant dueAt = dueAt(payment, methodConfig);

        if (Instant.now().isBefore(dueAt)) return;

        ChaosMode chaosMode = simulatorConfig.getChaosMode();

        switch (chaosMode) {
            case SUCCESS -> resolve(payment, true);
            case FAILURE -> resolve(payment, false);
            case TIMEOUT -> {
                log.debug("BankCallback simulator: Payment Time out");
            }
            case NORMAL, SLOW -> resolve(payment, shouldApprove(payment, methodConfig));
        }
    }

    private void resolve(Payment payment, boolean approve) {
        if (approve) {
            String bankRef = "SIM_BANK_REF" + RandomizerUtil.randomBase64(8);
            paymentService.resolveAuthorization(payment.getId(), true, bankRef, null, null);
        } else {
            paymentService.resolveAuthorization(payment.getId(), false, null, "SIM_BANK_ERROR_CODE", "Simulated bank declined");

        }
    }

    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig method) {
        int bucket = Math.abs(payment.getId().hashCode()) % 100;
        return bucket < method.getSuccessRate();
    }
    private Instant dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodConfig) {
        int range = methodConfig.getMaxDelaySeconds() - methodConfig.getMinDelaySeconds();
        int delaySeconds = methodConfig.getMinDelaySeconds() + Math.abs(payment.getId().hashCode()) % (range + 1);

        if (simulatorConfig.getChaosMode() == ChaosMode.SLOW)
            delaySeconds *= 2;

        return payment.getCreatedAt().plusSeconds(delaySeconds);

    }

}
