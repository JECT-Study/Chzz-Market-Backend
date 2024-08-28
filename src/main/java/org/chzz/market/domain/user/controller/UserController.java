package org.chzz.market.domain.user.controller;

import static org.chzz.market.common.filter.JWTFilter.AUTHORIZATION_HEADER;
import static org.chzz.market.common.filter.JWTFilter.BEARER_TOKEN_PREFIX;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.util.CookieUtil;
import org.chzz.market.domain.token.entity.TokenType;
import org.chzz.market.domain.token.service.TokenService;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity<?> completeRegistration(@LoginUser Long userId, @Valid @RequestBody UserCreateRequest userCreateRequest,
                                        HttpServletResponse response) {
        User user = userService.completeUserRegistration(userId, userCreateRequest);
        // 임시토큰 만료
        response.addCookie(CookieUtil.expireCookie(TokenType.TEMP.name()));
        // 리프레쉬 토큰 발급
        response.addCookie(CookieUtil.createTokenCookie(tokenService.createRefreshToken(user), TokenType.REFRESH));
        // 엑세스 토큰 발급
        response.setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + tokenService.createAccessToken(user));
        log.info("최종 회원가입 성공 userId = {}", userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<?> checkNickname(@NotBlank @Size(max = 15) @PathVariable String nickname) {
        return ResponseEntity.ok((userService.checkNickname(nickname)));
    }

    @PostMapping("/tokens/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshCookie = CookieUtil.getCookieByName(request, TokenType.REFRESH.name());
        Map<TokenType, String> newTokens = tokenService.reissue(refreshCookie);
        // 새로운 리프레쉬 토큰 발급
        response.addCookie(CookieUtil.createTokenCookie(newTokens.get(TokenType.REFRESH), TokenType.REFRESH));
        // 새로운 엑세스 토큰 발급
        response.setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + newTokens.get(TokenType.ACCESS));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshCookie = CookieUtil.getCookieByName(request, TokenType.REFRESH.name());
        tokenService.logout(refreshCookie);
        response.addCookie(CookieUtil.expireCookie(TokenType.REFRESH.name()));
        return ResponseEntity.ok().build();
    }
}
