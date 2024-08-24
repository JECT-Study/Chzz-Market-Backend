package org.chzz.market.domain.product.service;


import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.util.AuctionTestFactory;
import org.chzz.market.util.ProductTestFactory;
import org.chzz.market.util.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.chzz.market.domain.product.entity.Product.*;
import static org.chzz.market.domain.product.entity.Product.Category.*;
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

    private UpdateProductRequest updateRequest;
    private Product existingProduct;
    private User user;

    private ProductTestFactory productTestFactory;
    private AuctionTestFactory auctionTestFactory;
    private UserTestFactory userTestFactory;

    @BeforeEach
    void setUp() {
        productTestFactory = new ProductTestFactory();
        auctionTestFactory = new AuctionTestFactory();
        userTestFactory = new UserTestFactory();

        user = User.builder()
                .id(1L)
                .email("test@naver.com")
                .nickname("테스트 유저")
                .build();

        existingProduct = builder()
                .id(1L)
                .user(user)
                .name("기존 상품")
                .description("기존 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .images(new ArrayList<>())
                .build();

        updateRequest = UpdateProductRequest.builder()
                .userId(user.getId())
                .name("수정된 상품")
                .description("수정된 설명")
                .category(HOME_APPLIANCES)
                .minPrice(20000)
                .build();

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("사전 등록 상품 수정")
    class preRegister_Update {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 등록 상품 수정 성공 응답")
        void updateProduct_Success() {
            // given
            List<MultipartFile> images = createMockMultipartFiles();

            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            // when
            UpdateProductResponse response = productService.updateProduct(1L, updateRequest, images);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("수정된 상품");
            assertThat(response.description()).isEqualTo("수정된 설명");
            assertThat(response.category()).isEqualTo(HOME_APPLIANCES);
            assertThat(response.minPrice()).isEqualTo(20000);

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, times(1)).existsByProductId(1L);
            verify(imageRepository, times(1)).deleteAll(anyList());  // 이미지 삭제 확인
            verify(imageService, times(1)).uploadImages(anyList());  // 이미지 업로드 확인
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품으로 수정 시도 실패")
        void updateProduct_ProductNotFound() {
            // given
            List<MultipartFile> images = createMockMultipartFiles();
            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest, images))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다.");

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, never()).existsByProductId(anyLong());
            verify(imageService, never()).uploadImages(anyList());
            verify(imageRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("3. 이미 경매 등록된 상품 수정 시도 실패")
        void updateProduct_AlreadyInAuction() {
            // given
            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("이미 경매가 진행 중인 상품입니다.");

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, times(1)).existsByProductId(1L);
        }

        @Test
        @DisplayName("4. 이미지 없이 상품 정보만 수정 성공")
        void updateProduct_WithoutImages() {
            // given
            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            // when
            UpdateProductResponse response = productService.updateProduct(1L, updateRequest, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("수정된 상품");
            assertThat(response.description()).isEqualTo("수정된 설명");
            assertThat(response.category()).isEqualTo(HOME_APPLIANCES);
            assertThat(response.minPrice()).isEqualTo(20000);

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, times(1)).existsByProductId(1L);
            verify(imageRepository, never()).deleteAll(anyList());
            verify(imageService, never()).uploadImages(anyList());

        }

        @Test
        @DisplayName("5. 유효하지 않은 사용자가 상품 수정 시도 실패")
        void updateProduct_InvalidUser() {
            // given
            UpdateProductRequest invalidUserRequest = UpdateProductRequest.builder()
                    .userId(999L)
                    .name("수정된 상품")
                    .description("수정된 설명")
                    .category(HOME_APPLIANCES)
                    .minPrice(20000)
                    .build();

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(1L, invalidUserRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다.");

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(999L));
        }
    }

    private List<MultipartFile> createMockMultipartFiles() {
        MultipartFile mockFile1 = new MockMultipartFile(
                "testImage1.jpg", "testImage1.jpg", "image/jpeg", "test image content 1".getBytes());
        MultipartFile mockFile2 = new MockMultipartFile(
                "testImage2.jpg", "testImage2.jpg", "image/jpeg", "test image content 2".getBytes());
        return List.of(mockFile1, mockFile2);
    }
}
