package org.chzz.market.domain.user.controller;

import static org.chzz.market.domain.user.error.UserErrorCode.Const.CANNOT_DELETE_USER_DUE_TO_ONGOING_AUCTIONS;
import static org.chzz.market.domain.user.error.UserErrorCode.Const.CANNOT_DELETE_USER_DUE_TO_ONGOING_BIDS;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.springdoc.ApiExceptionExplanation;
import org.chzz.market.common.springdoc.ApiResponseExplanations;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.response.UserProfileResponse;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "users", description = "사용자 API")
public interface UserApi {

    @Operation(summary = "나의 customerKey 조회")
    ResponseEntity<Map<String, String>> getCustomerKey(Long userId);

    @Operation(summary = "나의 프로필 조회")
    ResponseEntity<UserProfileResponse> getUserProfileById(Long userId);

    @Operation(summary = "닉네임 중복 확인")
    ResponseEntity<NicknameAvailabilityResponse> checkNickname(@Length(min = 1, max = 15) String nickname);

    @Operation(summary = "회원가입 완료")
    ResponseEntity<Void> completeRegistration(Long userId, @Valid UserCreateRequest userCreateRequest,
                                              HttpServletResponse response);

    @Operation(summary = "프로필 수정")
    ResponseEntity<Void> updateUserProfile(Long userId, @Valid UpdateUserProfileRequest request);

    @Operation(summary = "JWT 토큰 재발급")
    ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "로그아웃")
    ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "회원탈퇴", description = "회원 탈퇴 시 진행 중인 경매나 입찰이 있을 경우 탈퇴가 불가능합니다.")
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = UserErrorCode.class, constant = CANNOT_DELETE_USER_DUE_TO_ONGOING_AUCTIONS, name = "진행 중인 경매가 있어 회원 탈퇴가 불가능한 경우"),
                    @ApiExceptionExplanation(value = UserErrorCode.class, constant = CANNOT_DELETE_USER_DUE_TO_ONGOING_BIDS, name = "진행 중인 입찰이 있어 회원 탈퇴가 불가능한 경우"),
            }
    )
    ResponseEntity<Void> deleteUser(@LoginUser Long userId, HttpServletRequest request, HttpServletResponse response);

}
