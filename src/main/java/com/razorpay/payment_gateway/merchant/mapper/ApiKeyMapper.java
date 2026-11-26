package com.razorpay.payment_gateway.merchant.mapper;

import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.payment_gateway.merchant.dto.response.ApiKeyResponse;
import com.razorpay.payment_gateway.merchant.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    ApiKeyResponse toResponse(ApiKey apiKey);
    ApiKeyCreateResponse toApiKeyCreateResponse(ApiKey apiKey);
}
