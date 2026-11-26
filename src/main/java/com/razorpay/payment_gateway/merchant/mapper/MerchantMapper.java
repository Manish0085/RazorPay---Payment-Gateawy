package com.razorpay.payment_gateway.merchant.mapper;

import com.razorpay.payment_gateway.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyResponse;
import com.razorpay.payment_gateway.merchant.dto.response.MerchantResponse;
import com.razorpay.payment_gateway.merchant.entity.ApiKey;
import com.razorpay.payment_gateway.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toEntityFromSignupRequest(MerchantSignupRequest request);
    MerchantResponse toResponse(Merchant merchant);
}
