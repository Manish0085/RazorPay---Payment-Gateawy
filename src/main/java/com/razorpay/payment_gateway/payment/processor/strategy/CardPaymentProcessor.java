package com.razorpay.payment_gateway.payment.processor.strategy;

import com.razorpay.payment_gateway.common.util.RandomizerUtil;
import com.razorpay.payment_gateway.payment.processor.PaymentProcessor;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    public static final String PAN_CARD_DECLINED = "40000000000000000002";
    public static final String PAN_CARD_EXPIRED = "40000000000000000069";
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        String pan = request.pan();
        if (PAN_CARD_DECLINED.equals(pan)) {
            log.warn("Card declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "CARD declined by bank");
        }

        if (PAN_CARD_EXPIRED.equals(pan)) {
            log.warn("Pan Card has expired");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Pan Card has expired");
        }

        String processorRef = "CARD_PROCESSOR" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorRef);

    }
}
