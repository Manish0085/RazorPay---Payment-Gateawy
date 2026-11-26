package com.razorpay.payment_gateway.payment.statemachine;

import com.razorpay.payment_gateway.common.enums.PaymentActor;
import com.razorpay.payment_gateway.common.enums.PaymentEvent;
import com.razorpay.payment_gateway.common.enums.PaymentStatus;
import com.razorpay.payment_gateway.common.exception.InvalidStateTransitionException;
import com.razorpay.payment_gateway.payment.entity.Payment;
import com.razorpay.payment_gateway.payment.entity.PaymentTransitionLog;
import com.razorpay.payment_gateway.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent event) {
        PaymentStatus next = paymentStateMachine.transition(payment.getPaymentStatus(), event);
        if (next == null) {
            throw new InvalidStateTransitionException(payment.getPaymentStatus().name(), event.name());
        }

        payment.setPaymentStatus(next);
        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getPaymentStatus())
                .event(event)
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) // TODO: fetch Merchant context to identify actor
                .occurredAt(Instant.now())
                .build();


        paymentTransitionLogRepository.save(log);
        return next;
    }
}
