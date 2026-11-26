package com.razorpay.payment_gateway.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
public class InvalidStateTransitionException extends RuntimeException{

    private final String fromState;
    private final String event;

    public InvalidStateTransitionException(String fromState, String event) {
        super("Invalid transition from " + fromState + " with event " + event);
        this.fromState = fromState;
        this.event = event;
    }
}
