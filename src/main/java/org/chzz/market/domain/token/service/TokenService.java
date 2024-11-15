package org.chzz.market.domain.token.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.aop.redisrock.DistributedLock;
import org.chzz.market.common.util.JWTUtil;
import org.chzz.market.domain.token.dto.TokenData;
import org.chzz.market.domain.token.entity.TokenType;
import org.chzz.market.domain.token.error.TokenErrorCode;
import org.chzz.market.domain.token.error.exception.TokenException;
import org.chzz.market.domain.token.repository.RefreshTokenRepository;
import org.chzz.market.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(User user) {
        return jwtUtil.createToken(user, TokenType.ACCESS);
    }

    public String createRefreshToken(User user) {
        String refreshToken = jwtUtil.createToken(user, TokenType.REFRESH);
        refreshTokenRepository.save(new TokenData(refreshToken, user.getId()));
        return refreshToken;
    }

    public String createTempToken(User user) {
        return jwtUtil.createToken(user, TokenType.TEMP);
    }

    @DistributedLock(key = "#refreshToken")
    public Map<TokenType, String> reissue(String refreshToken) {
        jwtUtil.validateToken(refreshToken, TokenType.REFRESH);
        Long userId = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenException(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND)).userId();
        String role = jwtUtil.getRole(refreshToken);
        User user = User.builder()
                .id(userId)
                .userRole(User.UserRole.valueOf(role))
                .build();
        refreshTokenRepository.deleteByToken(refreshToken);
        return Map.of(TokenType.ACCESS, createAccessToken(user), TokenType.REFRESH, createRefreshToken(user));
    }

    public void logout(String refreshToken) {
        jwtUtil.validateToken(refreshToken, TokenType.REFRESH);
        Long userId = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenException(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND)).userId();
        refreshTokenRepository.deleteByToken(refreshToken);
        log.info("사용자 ID {}: 로그아웃이 완료되었습니다.", userId);
    }
}
