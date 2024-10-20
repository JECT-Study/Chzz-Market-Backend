package org.chzz.market.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.response.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "users", description = "사용자 API")
public interface UserApi {

    @Operation(summary = "나의 customerKey 조회")
    ResponseEntity<Map<String, String>> getCustomerKey(Long userId);

    @Operation(summary = "나의 프로필 조회")
    ResponseEntity<UserProfileResponse> getUserProfileById(Long userId);

    @Operation(summary = "사용자 프로필 조회 (닉네임 기반) - 현재 사용 X")
    ResponseEntity<UserProfileResponse> getUserProfileByNickname(String nickname);

    @Operation(summary = "닉네임 중복 확인")
    ResponseEntity<NicknameAvailabilityResponse> checkNickname(String nickname);

    @Operation(summary = "회원가입 완료")
    ResponseEntity<Void> completeRegistration(Long userId, UserCreateRequest userCreateRequest,
                                              HttpServletResponse response);

    @Operation(summary = "프로필 수정")
    ResponseEntity<Void> updateUserProfile(Long userId, MultipartFile file, UpdateUserProfileRequest request);

    @Operation(summary = "JWT 토큰 재발급")
    ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "로그아웃")
    ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response);
}
