package org.chzz.market.domain.auction.type;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 경매종료 테스트 서비스 삭제필요
 */
@Service
@RequiredArgsConstructor
public class TestService {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Transactional
    public void test(Long userId, int seconds) {
        Random random = new Random();
        int randomIndex = random.nextInt(1000) + 1;  // 1부터 1000까지 랜덤 숫자 생성
        int randomIndex1 = random.nextInt(1000) + 1;  // 1부터 1000까지 랜덤 숫자 생성
        User user = userRepository.findById(userId).get();
        Product product = Product.builder()
                .name("테스트" + randomIndex)
                .description("test")
                .category(Category.ELECTRONICS)
                .user(user)
                .minPrice(10000)
                .build();
        productRepository.save(product);

        Image image1 = Image.builder()
                .cdnPath("https://picsum.photos/id/" + randomIndex + "/200/200")
                .product(product)
                .sequence(1)
                .build();

        Image image2 = Image.builder()
                .cdnPath("https://picsum.photos/id/" + randomIndex1 + "/200/200")
                .product(product)
                .sequence(2)
                .build();
        imageRepository.save(image1);
        imageRepository.save(image2);
        product.addImages(List.of(image1, image2));
        auctionRepository.save(Auction.builder()
                .status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusSeconds(seconds))
                .product(product)
                .build());
    }
}
