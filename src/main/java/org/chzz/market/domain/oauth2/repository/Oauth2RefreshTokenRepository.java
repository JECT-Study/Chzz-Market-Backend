package org.chzz.market.domain.oauth2.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.token.entity.TokenType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Oauth2RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void save(String providerType, String providerId, String refreshToken) {
        String key = generateKey(providerType, providerId);
        redisTemplate.opsForValue().set(key, refreshToken);
        redisTemplate.expire(key, TokenType.REFRESH.getExpirationTime(), TimeUnit.SECONDS);
    }

    public Optional<String> find(String providerType, String providerId) {
        String key = generateKey(providerType, providerId);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void delete(String providerType, String providerId) {
        String key = generateKey(providerType, providerId);
        redisTemplate.delete(key);
    }

    private String generateKey(String providerType, String providerId) {
        return providerType + ":" + providerId;
    }
}
