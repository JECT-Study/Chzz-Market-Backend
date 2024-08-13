package org.chzz.market.domain.product.service;

import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.chzz.market.domain.auction.entity.Auction.AuctionStatus.*;
import static org.chzz.market.domain.product.entity.Product.Category.ELECTRONICS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Auction auction;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@naver.com")
                .nickname("테스트 유저")
                .build();

        product = Product.builder()
                .id(1L)
                .user(user)
                .name("기존 상품")
                .description("기존 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .build();

        auction = Auction.builder()
                .id(1L)
                .product(product)
                .minPrice(10000)
                .status(PROCEEDING)
                .endDateTime(LocalDateTime.now().plusHours(24))
                .build();

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 상품 삭제 성공 응답")
        void deletePreRegisteredProduct_Success() {
            // given
            when(productRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(product));
            when(auctionRepository.findByProductId(anyLong())).thenReturn(Optional.empty());

            // when
            DeleteProductResponse response = productService.deleteProduct(1L, 1L);

            // then
            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.wasAuctioned()).isFalse();
            assertThat(response.notifiedParticipants()).isZero();
            verify(productRepository, times(1)).delete(product);
            verify(imageService, times(1)).deleteUploadImages(any());
        }

        @Test
        @DisplayName("2. 유효한 요청으로 경매 상품 삭제 성공 응답")
        void deleteAuctionedProduct_Success() {
            // Given
            when(productRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(product));
            when(auctionRepository.findByProductId(anyLong())).thenReturn(Optional.of(auction));

            // When
            DeleteProductResponse response = productService.deleteProduct(1L, 1L);

            // Then
            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.wasAuctioned()).isTrue();
            verify(productRepository, times(1)).delete(product);
            verify(auctionRepository, times(1)).delete(auction);
            verify(imageService, times(1)).deleteUploadImages(any());
        }

        @Test
        @DisplayName("3. 존재하지 않는 상품 삭제 시도")
        void deleteNonExistingProduct() {
            // Given
            when(productRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(1L, 1L))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("상품을 찾을 수 없습니다.");
        }


    }
}
