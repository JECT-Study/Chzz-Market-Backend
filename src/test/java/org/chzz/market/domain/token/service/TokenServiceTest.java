package org.chzz.market.domain.token.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import java.util.Optional;
import org.chzz.market.common.util.JWTUtil;
import org.chzz.market.domain.token.dto.TokenData;
import org.chzz.market.domain.token.entity.TokenType;
import org.chzz.market.domain.token.error.TokenErrorCode;
import org.chzz.market.domain.token.error.exception.TokenException;
import org.chzz.market.domain.token.repository.RefreshTokenRepository;
import org.chzz.market.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .userRole(User.UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("Access Token 생성 테스트")
    void createAccessToken_Success() {
        // given
        when(jwtUtil.createToken(user, TokenType.ACCESS)).thenReturn("access-token");

        // when
        String token = tokenService.createAccessToken(user);

        // then
        assertThat(token).isEqualTo("access-token");
    }

    @Test
    @DisplayName("Refresh Token 생성 및 저장 테스트")
    void createRefreshToken_Success() {
        // given
        when(jwtUtil.createToken(user, TokenType.REFRESH)).thenReturn("refresh-token");

        // when
        String token = tokenService.createRefreshToken(user);

        // then
        assertThat(token).isEqualTo("refresh-token");
        verify(refreshTokenRepository, times(1)).save(any(TokenData.class));
    }

    @Test
    @DisplayName("Temp Token 생성 테스트")
    void createTempToken_Success() {
        // given
        when(jwtUtil.createToken(user, TokenType.TEMP)).thenReturn("temp-token");

        // when
        String token = tokenService.createTempToken(user);

        // then
        assertThat(token).isEqualTo("temp-token");
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logout_Success() {
        // given
        Cookie refreshCookie = new Cookie("refresh-token", "refresh-token");
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(
                Optional.of(new TokenData("refresh-token", user.getId())));

        // when
        tokenService.logout(refreshCookie.getValue());

        // then
        verify(jwtUtil, times(1)).validateToken("refresh-token", TokenType.REFRESH);
        verify(refreshTokenRepository, times(1)).deleteByToken("refresh-token");
    }

    @Test
    @DisplayName("Refresh Token이 존재하지 않을 때 예외 발생 테스트")
    void reissue_ThrowsException_WhenTokenNotFound() {
        // given
        Cookie refreshCookie = new Cookie("refresh-token", "refresh-token");
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.empty());

        // when & then
        TokenException exception = assertThrows(TokenException.class, () -> tokenService.reissue(refreshCookie.getValue()));
        assertThat(exception.getErrorCode()).isEqualTo(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("로그아웃 시 Refresh Token이 존재하지 않을 때 예외 발생 테스트")
    void logout_ThrowsException_WhenTokenNotFound() {
        // given
        Cookie refreshCookie = new Cookie("refresh-token", "refresh-token");
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.empty());

        // when & then
        TokenException exception = assertThrows(TokenException.class, () -> tokenService.logout(refreshCookie.getValue()));
        assertThat(exception.getErrorCode()).isEqualTo(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}
