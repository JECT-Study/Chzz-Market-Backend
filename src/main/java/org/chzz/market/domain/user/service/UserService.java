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
    public UserProfileResponse getUserProfile(String nickname) {
        User user = userRepository.findByNickname(nickname).orElseThrow(() -> new UserException(USER_NOT_FOUND));

        List<AuctionParticipationResponse> participations = auctionRepository.getAuctionParticipations(user.getId());
        ParticipationCountsResponse counts = calculateParticipationCounts(user.getId(), participations);

        long preRegisterCount = productRepository.countPreRegisteredProductsByUserId(user.getId());
        long registeredAuctionCount = auctionRepository.countByProductUserId(user.getId());

        return UserProfileResponse.of(user, counts, preRegisterCount, registeredAuctionCount);
    }

    /**
     * 사용자 프로필 조회 (유저 ID 기반)
     * @param userId 유저 ID
     * @return 사용자 프로필 응답
     */
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));

        List<AuctionParticipationResponse> participations = auctionRepository.getAuctionParticipations(user.getId());
        ParticipationCountsResponse counts = calculateParticipationCounts(userId, participations);

        long preRegisterCount = productRepository.countPreRegisteredProductsByUserId(userId);
        long registeredAuctionCount = auctionRepository.countByProductUserId(userId);

        return UserProfileResponse.of(user, counts, preRegisterCount, registeredAuctionCount);
    }

    public NicknameAvailabilityResponse checkNickname(String nickname) {
        return new NicknameAvailabilityResponse(userRepository.findByNickname(nickname).isEmpty());
    }

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
}
