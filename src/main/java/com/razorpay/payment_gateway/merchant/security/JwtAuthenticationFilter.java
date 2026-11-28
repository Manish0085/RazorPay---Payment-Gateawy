package com.razorpay.payment_gateway.merchant.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtUtil jwtUtil;
    HandlerExceptionResolver handlerExceptionResolver;

    MerchantContext merchantContext;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            log.info("Incoming Request: {}", request.getRequestURI());

            final String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorizationHeader.split("Bearer ")[1];

            Claims claims = jwtUtil.verifyAccessToken(token);

            if (claims != null && SecurityContextHolder.getContext() != null) {
                var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + jwtUtil.extractRole(claims)))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

                merchantContext.setMerchantId(UUID.fromString(jwtUtil.extractMerchantId(claims)));
            }
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
