package org.chzz.market.domain.auctionv2.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.dto.ImageUploadEvent;
import org.chzz.market.domain.auctionv2.dto.RegisterRequest;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuctionRegistrationService implements RegistrationService {
    private final AuctionV2Repository auctionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void register(final Long userId, RegisterRequest request, final List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        AuctionV2 auction = createAuction(request, user);

        auctionRepository.save(auction);

        eventPublisher.publishEvent(new ImageUploadEvent(auction,images));
    }

    private AuctionV2 createAuction(final RegisterRequest request, final User user) {
        return AuctionV2.builder()
                .name(request.getProductName())
                .minPrice(request.getMinPrice())
                .description(request.getDescription())
                .seller(user)
                .status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();
    }
}