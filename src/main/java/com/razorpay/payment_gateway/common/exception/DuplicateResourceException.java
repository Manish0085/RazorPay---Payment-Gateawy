package com.razorpay.payment_gateway.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@Getter
public class DuplicateResourceException extends RuntimeException{

    private final String errorCode;

    public DuplicateResourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
