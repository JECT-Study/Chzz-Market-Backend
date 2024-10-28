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
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.response.UserProfileResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.service.UserService;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController implements UserApi {
    private final UserService userService;
    private final TokenService tokenService;

    /**
     * 내 customerKey 조회
     */
    @Override
    @GetMapping("/customer-key")
    public ResponseEntity<Map<String, String>> getCustomerKey(@LoginUser Long userId) {
        String customerKey = userService.getCustomerKey(userId);
        return ResponseEntity.ok(Map.of("customerKey", customerKey));
    }

    /**
     * 사용자 프로필 조회 (유저 ID 기반)
     */
    @Override
    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfileById(@LoginUser Long userId) {
        return ResponseEntity.ok(userService.getUserProfileById(userId));
    }

    /**
     * 사용자 프로필 조회 (닉네임 기반) 현재 사용 X
     */
    @Override
    @GetMapping("/{nickname}")
    public ResponseEntity<UserProfileResponse> getUserProfileByNickname(@PathVariable String nickname) {
        return ResponseEntity.ok(userService.getUserProfileByNickname(nickname));
    }

    /**
     * 닉네임 중복 확인
     */
    @Override
    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<NicknameAvailabilityResponse> checkNickname(@PathVariable @Length(min = 1, max = 15) String nickname) {
        return ResponseEntity.ok((userService.checkNickname(nickname)));
    }

    /**
     * 회원가입 완료
     */
    @Override
    @PostMapping
    public ResponseEntity<Void> completeRegistration(@LoginUser Long userId,
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

    /**
     * 내 프로필 수정
     */
    @Override
    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserProfile(
            @LoginUser Long userId,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart @Valid UpdateUserProfileRequest request) {
        userService.updateUserProfile(userId, file, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 토큰 재발급
     */
    @Override
    @PostMapping("/tokens/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookieByNameOrThrow(request, TokenType.REFRESH.name());
        Map<TokenType, String> newTokens = tokenService.reissue(refreshToken);
        // 새로운 리프레쉬 토큰 발급
        CookieUtil.createTokenCookie(response, newTokens.get(TokenType.REFRESH), TokenType.REFRESH);
        // 새로운 엑세스 토큰 발급
        response.setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + newTokens.get(TokenType.ACCESS));
        return ResponseEntity.ok().build();
    }

    /**
     * 로그아웃
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookieByNameOrThrow(request, TokenType.REFRESH.name());
        tokenService.logout(refreshToken);
        CookieUtil.expireCookie(response, TokenType.REFRESH.name());
        return ResponseEntity.ok().build();
    }
}
