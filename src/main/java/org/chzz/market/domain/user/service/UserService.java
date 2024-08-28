package org.chzz.market.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.user.dto.ParticipationCountsResponse;
import org.chzz.market.domain.user.dto.UserProfileResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;

    public UserProfileResponse getUserProfile(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> {
                    log.info("닉네임 {}에 해당하는 사용자를 찾을 수 없습니다.", nickname);
                    return new UserException(USER_NOT_FOUND);
                });

        ParticipationCountsResponse counts = auctionRepository.getParticipationCounts(user.getId());

        return UserProfileResponse.of(user, counts);
    }
}
