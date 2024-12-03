package org.chzz.market.domain.user.service;

import static org.chzz.market.domain.user.error.UserErrorCode.NICKNAME_DUPLICATION;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.response.ParticipationCountsResponse;
import org.chzz.market.domain.user.dto.response.UserProfileResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionQueryRepository auctionQueryRepository;
    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    /**
     * 사용자 프로필 조회 (유저 ID 기반)
     */
    public UserProfileResponse getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        long preAuctionCount = auctionRepository.countBySellerIdAndStatusIn(userId, AuctionStatus.PRE);
        long officialAuctionCount = auctionRepository.countBySellerIdAndStatusIn(userId, AuctionStatus.PROCEEDING,
                AuctionStatus.ENDED);
        ParticipationCountsResponse counts = auctionQueryRepository.getParticipationCounts(userId);
        return UserProfileResponse.of(user, counts, preAuctionCount, officialAuctionCount);
    }

    /**
     * 닉네임 이용가능 체크
     */
    public NicknameAvailabilityResponse checkNickname(String nickname) {
        return new NicknameAvailabilityResponse(userRepository.findByNickname(nickname).isEmpty());
    }

    /**
     * customerKey 조회
     */
    public String getCustomerKey(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND))
                .getCustomerKey().toString();
    }

    /**
     * 사용자 등록
     */
    @Transactional
    public User completeUserRegistration(Long userId, UserCreateRequest userCreateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
        userRepository.findByNickname(userCreateRequest.getNickname()).ifPresent(user1 -> {
            throw new UserException(NICKNAME_DUPLICATION);
        });
        user.createUser(userCreateRequest);
        return user;
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public void updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        // 유저 유효성 검사
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        // 닉네임 중복 검사 (본인 제외)
        userRepository.findByNickname(request.getNickname())
                .filter(user -> !user.equals(existingUser))
                .ifPresent(user -> {
                    throw new UserException(NICKNAME_DUPLICATION);
                });
        String profileImageUrl = handleProfileImage(request.getObjectKey(), request.getUseDefaultImage(),
                existingUser.getProfileImageUrl());
        existingUser.updateProfile(request, profileImageUrl);
    }

    /**
     * 회원 프로필 이미지 변경
     */
    private String handleProfileImage(String objectKey, Boolean useDefaultImage, String currentProfileImageUrl) {
        if (useDefaultImage) {
            return null;  // 기존 이미지를 삭제하고 기본이미지로 (null)
        }
        return cloudfrontDomain + "/" + objectKey;
    }
}
