package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.chzz.market.domain.auction.entity.Auction.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService{
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request, AuctionStatus initialStatus) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

//        Product product = request.toProductEntity();
        Product product = createProduct(request, user);
        productRepository.save(product);

//        Auction auction = request.toAuctionEntity(product, initialStatus);
        Auction auction = createAuction(product, request, initialStatus);
        auctionRepository.save(auction);

        // 이미지 처리
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> imageUrls = imageService.uploadImages(request.getImages());
            imageService.saveProductImageEntities(product, imageUrls);
        }

        return new RegisterResponse(product.getId(), auction.getId(), auction.getStatus(), "success");
    }

    private Product createProduct(RegisterRequest request, User user) {
        return Product.builder()
                .user(user)
                .name(request.getProductName())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();
    }

    private Auction createAuction(Product product, RegisterRequest request, AuctionStatus status) {
        return Auction.builder()
                .product(product)
                .minPrice(request.getMinPrice())
                .status(status)
                .build();
    }

}
