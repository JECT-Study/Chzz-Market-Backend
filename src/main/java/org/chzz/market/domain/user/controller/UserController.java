package org.chzz.market.domain.user.controller;

import static org.chzz.market.common.filter.JWTFilter.AUTHORIZATION_HEADER;
import static org.chzz.market.common.filter.JWTFilter.BEARER_TOKEN_PREFIX;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.util.CookieUtil;
import org.chzz.market.domain.token.entity.TokenType;
import org.chzz.market.domain.token.service.TokenService;
import org.chzz.market.domain.user.dto.response.UpdateProfileResponse;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.UserProfileResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
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

    /*
     * 회원가입 완료
     */
    @PostMapping
    public ResponseEntity<?> completeRegistration(@LoginUser Long userId,
                                                  @Valid @RequestBody UserCreateRequest userCreateRequest,
                                                  HttpServletResponse response) {
        User user = userService.completeUserRegistration(userId, userCreateRequest);
        // 임시토큰 만료
        CookieUtil.expireCookie(response, TokenType.TEMP.name());
        // 리프레쉬 토큰 발급
        CookieUtil.createTokenCookie(response, tokenService.createRefreshToken(user), TokenType.REFRESH);
        // 엑세스 토큰 발급
        response.setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + tokenService.createAccessToken(user));
        log.info("최종 회원가입 성공 userId = {}", userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer-key")
    public ResponseEntity<String> getCustomerKey(@LoginUser Long userId) {
        return ResponseEntity.ok(userService.getCustomerKey(userId));
    }

    /**
     * 내 프로필 수정
     */
    @PostMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> updateUserProfile(
            @LoginUser Long userId,
            @RequestBody @Valid UpdateUserProfileRequest request) {
        UpdateProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /*
     * 사용자 프로필 조회 (유저 ID 기반)
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfileById(@LoginUser Long userId) {
        return ResponseEntity.ok(userService.getUserProfileById(userId));
    }

    /*
     * 사용자 프로필 조회 (닉네임 기반)
     */
    @GetMapping("/{nickname}")
    public ResponseEntity<UserProfileResponse> getUserProfileByNickname(@PathVariable String nickname) {
        return ResponseEntity.ok(userService.getUserProfileByNickname(nickname));
    }

    /*
     * 닉네임 중복 확인
     */
    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<?> checkNickname(@PathVariable String nickname) {
        return ResponseEntity.ok((userService.checkNickname(nickname)));
    }

    /*
     * 토큰 재발급
     */
    @PostMapping("/tokens/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshCookie = CookieUtil.getCookieByName(request, TokenType.REFRESH.name());
        Map<TokenType, String> newTokens = tokenService.reissue(refreshCookie);
        // 새로운 리프레쉬 토큰 발급
        CookieUtil.createTokenCookie(response, newTokens.get(TokenType.REFRESH), TokenType.REFRESH);
        // 새로운 엑세스 토큰 발급
        response.setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + newTokens.get(TokenType.ACCESS));
        return ResponseEntity.ok().build();
    }

    /*
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshCookie = CookieUtil.getCookieByName(request, TokenType.REFRESH.name());
        tokenService.logout(refreshCookie);
        CookieUtil.expireCookie(response, TokenType.REFRESH.name());
        return ResponseEntity.ok().build();
    }
}