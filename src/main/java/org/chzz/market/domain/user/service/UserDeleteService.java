package org.chzz.market.domain.user.service;

import static org.chzz.market.domain.user.error.UserErrorCode.CANNOT_DELETE_USER_DUE_TO_ONGOING_AUCTIONS;
import static org.chzz.market.domain.user.error.UserErrorCode.CANNOT_DELETE_USER_DUE_TO_ONGOING_BIDS;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.user.dto.UserDeletedEvent;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeleteService {
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionQueryRepository auctionQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        // 1. 탈퇴 가능 여부 체크
        checkDeletable(userId);

        // 2. 정보 삭제 전 소셜 로그인 연결 정보 저장
        ProviderType type = user.getProviderType();
        String providerId = user.getProviderId();

        // 3. 정보 삭제
        user.anonymize();

        // 4. 소셜 로그인 연결 끊기
        eventPublisher.publishEvent(new UserDeletedEvent(type, providerId));
    }

    private void checkDeletable(Long userId) {
        //1. 현재 등록한 경매 중 진행중이 있는지
        long proceedingAuctionCount = auctionRepository.countBySellerIdAndStatusIn(userId, AuctionStatus.PROCEEDING);
        if (proceedingAuctionCount > 0) {
            throw new UserException(CANNOT_DELETE_USER_DUE_TO_ONGOING_AUCTIONS);
        }
        //2. 현재 입찰 진행 중인 경매 가 있는지
        long proceedingBidCount = auctionQueryRepository.countProceedingAuctionsByUserId(userId);
        if (proceedingBidCount > 0) {
            throw new UserException(CANNOT_DELETE_USER_DUE_TO_ONGOING_BIDS);
        }
    }
}
