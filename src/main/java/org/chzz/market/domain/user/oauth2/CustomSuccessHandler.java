package org.chzz.market.domain.user.oauth2;

import static org.chzz.market.common.util.CookieUtil.createTokenCookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.token.entity.TokenType;
import org.chzz.market.domain.token.service.TokenService;
import org.chzz.market.domain.user.dto.CustomUserDetails;
import org.chzz.market.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${oauth2.redirect-url}")
    private String oauth2RedirectUrl;

    @Value("${oauth2.additional-info-url}")
    private String oauth2RedirectAdditionalInfoUrl;

    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        if (user.isTempUser()) {
            // 임시 토큰 발급
            String tempToken = tokenService.createTempToken(user);
            response.addCookie(createTokenCookie(tempToken, TokenType.TEMP));

            // 추가 입력 페이지로 리다이렉트
            response.sendRedirect(oauth2RedirectAdditionalInfoUrl);
            log.info("임시 유저 인증 성공: user ID: {}", user.getId());
        } else {
            // 리프레쉬 토큰 발급
            String refresh = tokenService.createRefreshToken(user);
            response.addCookie(createTokenCookie(refresh, TokenType.REFRESH));

            String redirectUrl = oauth2RedirectUrl + "?status=success";
            response.sendRedirect(redirectUrl);
            log.info("소셜로그인 성공 user ID: {}", user.getId());
        }
    }
}
