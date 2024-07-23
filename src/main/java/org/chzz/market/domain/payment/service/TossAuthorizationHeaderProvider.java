package org.chzz.market.domain.payment.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TossAuthorizationHeaderProvider extends PaymentAuthorizationHeaderProvider {
    private static final String PREFIX = "Basic ";

    private final String secretKey;
    private final String clientKey;

    public TossAuthorizationHeaderProvider(
            @Value("${payment.toss.security-key}")
            String secretKey,
            @Value("${payment.toss.client-key}")
            String clientKey) {
        this.secretKey = secretKey;
        this.clientKey = clientKey;
    }

    @Override
    public String getAuthorizationHeader() {
        String encodedSecretKey = Base64.getEncoder().encodeToString((secretKey + ":")
                        .getBytes(StandardCharsets.UTF_8));
        return PREFIX.concat(encodedSecretKey);
    }
}