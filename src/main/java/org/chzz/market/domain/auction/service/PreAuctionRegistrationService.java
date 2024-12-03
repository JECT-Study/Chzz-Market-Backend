package org.chzz.market.domain.auction.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.ImageUploadEvent;
import org.chzz.market.domain.auction.dto.request.RegisterRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ObjectKeyValidator;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreAuctionRegistrationService implements RegistrationService {
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectKeyValidator objectKeyValidator;

    @Override
    @Transactional
    public void register(final Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Auction auction = createAuction(request, user);

        auctionRepository.save(auction);
        List<String> objectKeys = request.objectKeys();
        objectKeys.forEach(objectKeyValidator::validate);

        eventPublisher.publishEvent(new ImageUploadEvent(auction, objectKeys));
    }

    private Auction createAuction(final RegisterRequest request, final User user) {
        return Auction.builder()
                .name(request.auctionName())
                .minPrice(request.minPrice())
                .category(request.category())
                .description(request.description())
                .seller(user)
                .status(AuctionStatus.PRE)
                .build();
    }
}
