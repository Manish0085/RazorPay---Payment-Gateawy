package com.razorpay.payment_gateway.payment.processor.strategy;

import com.razorpay.payment_gateway.common.util.RandomizerUtil;
import com.razorpay.payment_gateway.payment.processor.PaymentProcessor;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.payment_gateway.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL = "fail@okaxis";

        String bankCode = request.methodDetails() != null?
                request.methodDetails().get("vpa").toString(): null;

        if (BANK_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "Bank rejected the transaction registration");
        }

        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        String bankRef = "BANK_REF" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);

    }
}
