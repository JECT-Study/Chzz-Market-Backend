package org.chzz.market.domain.user.service;

import static org.chzz.market.domain.auction.type.AuctionStatus.*;
import static org.chzz.market.domain.user.error.UserErrorCode.NICKNAME_DUPLICATION;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.response.AuctionParticipationResponse;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.dto.response.UpdateProfileResponse;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.response.ParticipationCountsResponse;
import org.chzz.market.domain.user.dto.response.UserProfileResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;

    /**
     * 사용자 등록
     *
     * @param userCreateRequest 사용자 생성 요청
     * @return 사용자 엔티티
     */
    @Transactional
    public User completeUserRegistration(Long userId, UserCreateRequest userCreateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
        if (userRepository.findByNickname(userCreateRequest.getNickname()).isPresent()) {
            throw new UserException(NICKNAME_DUPLICATION);
        }
        user.createUser(userCreateRequest);
        user.addBankAccount(userCreateRequest.toBankAccount());
        return user;
    }

    /**
     * 사용자 프로필 조회 (닉네임 기반)
     *
     * @param nickname 닉네임
     * @return 사용자 프로필 응답
     */
    public UserProfileResponse getUserProfileByNickname(String nickname) {
        return getUserProfileInternal(findUserByNickname(nickname));
    }

    /**
     * 사용자 프로필 조회 (유저 ID 기반)
     * @param userId 유저 ID
     * @return 사용자 프로필 응답
     */
    public UserProfileResponse getUserProfileById(Long userId) {
        return getUserProfileInternal(findUserById(userId));
    }

    public NicknameAvailabilityResponse checkNickname(String nickname) {
        return new NicknameAvailabilityResponse(userRepository.findByNickname(nickname).isEmpty());
    }

    /**
     * 내 프로필 수정
     *
     * @param userId 유저 ID
     * @param request 프로필 수정 요청
     * @return 프로필 수정 응답
     */
    @Transactional
    public UpdateProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        // 유저 유효성 검사
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        userRepository.findByNickname(request.getNickname()).ifPresent(user -> {
            if(!existingUser.equals(user)) { // 본인 닉네일시
                throw new UserException(NICKNAME_DUPLICATION);
            }
        });

        // 프로필 정보 업데이트
        existingUser.updateProfile(
                request.getNickname(),
                request.getBio(),
                request.getLink()
        );
        return UpdateProfileResponse.from(existingUser);
    }

    /*
     * 내 프로필 조회
     */
    private UserProfileResponse getUserProfileInternal(User user) {
        long preRegisterCount = productRepository.countPreRegisteredProductsByUserId(user.getId());
        long registeredAuctionCount = auctionRepository.countByProductUserId(user.getId());

        ParticipationCountsResponse counts = new ParticipationCountsResponse(
                user.getOngoingAuctionCount(),
                user.getSuccessfulBidCount(),
                user.getFailedBidCount()
        );

        return UserProfileResponse.of(user, counts, preRegisterCount, registeredAuctionCount);
    }

    /*
     * 경매 참여 횟수 계산
     */
    private ParticipationCountsResponse calculateParticipationCounts(Long userId, List<AuctionParticipationResponse> participations) {
        long ongoingAuctionCount = 0;
        long successfulBidCount = 0;
        long failedBidCount = 0;

        for (AuctionParticipationResponse participation : participations) {
            if (participation.status() == PROCEEDING) {
                ongoingAuctionCount += participation.count();
            } else {
                if (userId.equals(participation.winnerId())) {
                    successfulBidCount += participation.count();
                } else {
                    failedBidCount += participation.count();
                }
            }
        }

        return new ParticipationCountsResponse(
                ongoingAuctionCount,
                successfulBidCount,
                failedBidCount
        );
    }

    /*
     * 닉네임으로 사용자 조회
     */
    private User findUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }

    /*
     * ID로 사용자 조회
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }
}
