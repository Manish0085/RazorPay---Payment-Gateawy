package com.razorpay.payment_gateway.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static String randomBase64(int len){
        byte[] buf = new byte[len/2];
        SECURE_RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
