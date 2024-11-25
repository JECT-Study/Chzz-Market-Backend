package org.chzz.market.domain.auction.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.AuctionRegistrationEvent;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionTestService {
    private final AuctionRepository auctionRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void test(Long userId, int seconds) {
        Random random = new Random();
        int randomIndex = random.nextInt(1000) + 1;  // 1부터 1000까지 랜덤 숫자 생성
        int randomIndex1 = random.nextInt(1000) + 1;  // 1부터 1000까지 랜덤 숫자 생성
        User user = userRepository.findById(userId).get();
        Auction auction = Auction.builder()
                .name("테스트" + randomIndex)
                .description("test")
                .category(Category.ELECTRONICS)
                .seller(user)
                .minPrice(10000)
                .status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusSeconds(seconds))
                .build();
        auctionRepository.save(auction);

        Image image1 = Image.builder()
                .cdnPath("https://picsum.photos/id/" + randomIndex + "/200/200")
                .auction(auction)
                .sequence(1)
                .build();

        Image image2 = Image.builder()
                .cdnPath("https://picsum.photos/id/" + randomIndex1 + "/200/200")
                .auction(auction)
                .sequence(2)
                .build();
        imageRepository.save(image1);
        imageRepository.save(image2);
        auction.addImages(List.of(image1, image2));
        eventPublisher.publishEvent(new AuctionRegistrationEvent(auction.getId(), auction.getEndDateTime()));
    }
}
