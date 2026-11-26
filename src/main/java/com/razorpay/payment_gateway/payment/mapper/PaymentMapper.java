package com.razorpay.payment_gateway.payment.mapper;

import com.razorpay.payment_gateway.payment.dto.response.PaymentResponse;
import com.razorpay.payment_gateway.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);


}
