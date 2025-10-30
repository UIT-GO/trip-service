package com.example.trip_service.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {
    @Value("${jwt.secretkey}")
    private String secretKey;
    @Value("${jwt.header}")
    private String jwtHeader;

    public String getSecretKey() {
        return secretKey;
    }

    public String getJwtHeader() {
        return jwtHeader;
    }
}
