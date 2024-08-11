package org.chzz.market.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.auction.entity.Auction.AuctionStatus.*;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.*;
import static org.chzz.market.domain.product.entity.Product.Category.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auction.dto.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;
import org.chzz.market.domain.auction.dto.StartResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {
    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private AuctionService auctionService;

    private ProductTestFactory productTestFactory;
    private AuctionTestFactory auctionTestFactory;
    private UserTestFactory userTestFactory;

    private RegisterRequest validRequest, InvalidRequest;

    @BeforeEach
    void setUp() {
        productTestFactory = new ProductTestFactory();
        auctionTestFactory = new AuctionTestFactory();
        userTestFactory = new UserTestFactory();

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("상품 사전 등록 테스트")
    class PreRegisterTest {

        @Test
        @DisplayName("1. 유효한 요청으로 상품 사전 등록 성공 응답")
        void preRegister_Success() {
            // given
            Long productId = 1L;
            Long auctionId = 1L;
            User user = UserTestFactory.createUser(1L, "seller", "test@naver.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                ReflectionTestUtils.setField(product, "id", productId);  // ID 설정
                return product;
            });
            when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> {
                Auction auction = invocation.getArgument(0);
                ReflectionTestUtils.setField(auction, "id", auctionId);  // Auction ID 설정
                return auction;
            });
            when(imageService.uploadImages(anyList())).thenReturn(List.of("image1.jpg", "image2.jpg"));

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest validRequest = RegisterRequest.builder()
                    .userId(user.getId())
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PENDING)
                    .images(images)
                    .build();

            // when
            RegisterResponse response = auctionService.register(validRequest);

            // then
            assertNotNull(response);
            assertEquals(productId, response.productId());
            assertEquals(auctionId, response.auctionId());
            assertEquals(PENDING, response.status());
            verify(productRepository, times(1)).save(any(Product.class));
            verify(auctionRepository, times(1)).save(any(Auction.class));
            verify(imageService, times(1)).uploadImages(anyList());
        }

        @Test
        @DisplayName("2. 존재하지 않는 사용자로 상품 사전 등록 실패")
        void preRegister_UserNotFound() {
            // Given
            Long productId = 1L;
            Long auctionId = 1L;

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest invalidRequest = RegisterRequest.builder()
                    .userId(999L)
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PENDING)
                    .images(images)
                    .build();

            // When & Then
            assertThrows(UserException.class, () -> {
                auctionService.register(invalidRequest);
            });

            // verify
            verify(productRepository, never()).save(any(Product.class));
            verify(auctionRepository, never()).save(any(Auction.class));
            verify(imageService, never()).uploadImages(anyList());

        }
    }

    @Nested
    @DisplayName("상품 경매 등록 테스트")
    class RegisterAuctionTest {

        @Test
        @DisplayName("1. 유효한 요청으로 경매 상품 등록 성공 응답")
        void registerAuction_Success() {
            // given
            Long productId = 1L;
            Long auctionId = 1L;
            User user = UserTestFactory.createUser(1L, "seller", "test@naver.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                ReflectionTestUtils.setField(product, "id", productId);  // ID 설정
                return product;
            });
            when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> {
                Auction auction = invocation.getArgument(0);
                ReflectionTestUtils.setField(auction, "id", auctionId);  // Auction ID 설정
                return auction;
            });
            when(imageService.uploadImages(anyList())).thenReturn(List.of("image1.jpg", "image2.jpg"));

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest validRequest = RegisterRequest.builder()
                    .userId(user.getId())
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PROCEEDING)
                    .images(images)
                    .build();

            // when
            RegisterResponse response = auctionService.register(validRequest);

            // then
            assertNotNull(response);
            assertEquals(productId, response.productId());  // Product ID 검증
            assertEquals(auctionId, response.auctionId());  // Auction ID 검증
            assertEquals(PROCEEDING, response.status());
            verify(productRepository, times(1)).save(any(Product.class));
            verify(auctionRepository, times(1)).save(any(Auction.class));
            verify(imageService, times(1)).uploadImages(anyList());
        }

        @Test
        @DisplayName("2. 존재하지 않는 사용자로 상품 경매 등록 실패")
        void registerAuction_UserNotFound() {
            // Given
            Long productId = 1L;
            Long auctionId = 1L;

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest invalidRequest = RegisterRequest.builder()
                    .userId(999L)
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PROCEEDING)
                    .images(images)
                    .build();

            // When & Then
            assertThrows(UserException.class, () -> {
                auctionService.register(invalidRequest);
            });

            // verify
            verify(productRepository, never()).save(any(Product.class));
            verify(auctionRepository, never()).save(any(Auction.class));
            verify(imageService, never()).uploadImages(anyList());

        }
    }

    @Nested
    @DisplayName("사전 등록 된 상품 경매 등록 상품으로 전환 테스트")
    class StartAuctionTest {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 등록 된 상품 경매 등록 전환 성공 응답")
        void startAuction_Success() {
            // given
            Long auctionId = 1L;
            Long userId = 1L;
            User user = UserTestFactory.createUser(1L, "seller", "test@naver.com");

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest validRequest = RegisterRequest.builder()
                    .userId(user.getId())
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PENDING)
                    .images(images)
                    .build();

            LocalDateTime startTime = LocalDateTime.now();
            Product product = ProductTestFactory.createProduct(validRequest, user);
            Auction pendingAuction = AuctionTestFactory.createAuction(product, validRequest, PENDING);
            ReflectionTestUtils.setField(pendingAuction, "id", auctionId);
            ReflectionTestUtils.setField(pendingAuction, "status", PENDING);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(pendingAuction));
            when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.<Auction>getArgument(0));

            // when
            StartResponse response = auctionService.startAuction(auctionId, userId);

            // Then
            assertNotNull(response);
            assertEquals(auctionId, response.auctionId());
            assertEquals(product.getId(), response.productId());
            assertEquals(PROCEEDING, response.status());
            assertNotNull(response.endTime());
            assertTrue(response.endTime().isAfter(startTime));

            verify(auctionRepository).findById(auctionId);
            verify(auctionRepository).save(any(Auction.class));
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품 ID로 전환 시도 실패")
        void startAuction_NotFound() {
            // Given
            Long nonExistentAuctionId = 999L;
            Long userId = 1L;
            when(auctionRepository.findById(nonExistentAuctionId)).thenReturn(Optional.empty());

            // When & Then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.startAuction(nonExistentAuctionId, userId));

            assertThrows(AuctionException.class, () -> auctionService.startAuction(nonExistentAuctionId, userId));

            assertEquals(AUCTION_NOT_FOUND, exception.getErrorCode());
            verify(auctionRepository, never()).save(any(Auction.class));
        }

        @Test
        @DisplayName("3. 이미 진행 중인 경매 상품 전환 시도 실패")
        void startAuction_AlreadyProceeding() {
            // given
            Long auctionId = 1L;
            Long userId = 1L;
            User user = UserTestFactory.createUser(1L, "seller", "test@naver.com");

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest validRequest = RegisterRequest.builder()
                    .userId(user.getId())
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PROCEEDING)
                    .images(images)
                    .build();

            LocalDateTime startTime = LocalDateTime.now();
            Product product = ProductTestFactory.createProduct(validRequest, user);
            Auction proceedingAuction = AuctionTestFactory.createAuction(product, validRequest, PROCEEDING);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(proceedingAuction));

            // When & Then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.startAuction(auctionId, userId));
            assertEquals(INVALID_AUCTION_STATE, exception.getErrorCode());

            verify(auctionRepository, never()).save(any(Auction.class));
        }

        @Test
        @DisplayName("4. 취소된 경매 상품 전환 시도")
        void startAuction_Cancelled() {
            // given
            Long auctionId = 1L;
            Long userId = 1L;
            User user = UserTestFactory.createUser(1L, "seller", "test@naver.com");

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest validRequest = RegisterRequest.builder()
                    .userId(user.getId())
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(CANCELLED)
                    .images(images)
                    .build();

            Product product = ProductTestFactory.createProduct(validRequest, user);
            Auction cancelledAuction = AuctionTestFactory.createAuction(product, validRequest, CANCELLED);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(cancelledAuction));

            // When & Then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.startAuction(auctionId, userId));
            assertEquals(INVALID_AUCTION_STATE, exception.getErrorCode());

            verify(auctionRepository, never()).save(any(Auction.class));
        }

        @Test
        @DisplayName("5. 종료된 경매 상품 전환 시도 실패")
        void startAuction_Ended() {
            // Given
            // given
            Long auctionId = 1L;
            Long userId = 1L;
            User user = UserTestFactory.createUser(1L, "seller", "test@naver.com");

            MultipartFile mockFile = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            MultipartFile mockFile2 = new MockMultipartFile(
                    "testImage.jpg",
                    "testImage.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            List<MultipartFile> images = List.of(mockFile, mockFile2);

            RegisterRequest validRequest = RegisterRequest.builder()
                    .userId(user.getId())
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(ENDED)
                    .images(images)
                    .build();

            Product product = ProductTestFactory.createProduct(validRequest, user);
            Auction endedAuction = AuctionTestFactory.createAuction(product, validRequest, ENDED);

            when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(endedAuction));

            // When & Then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.startAuction(auctionId, userId));
            assertEquals(INVALID_AUCTION_STATE, exception.getErrorCode());

            verify(auctionRepository, never()).save(any(Auction.class));
        }
    }

    @Test
    @DisplayName("경매 상세 조회 - 값이 채워진 경우 예외 발생 안함")
    public void testGetAuctionDetails_ExistingAuction_NoException() {
        // given
        Long existingAuctionId = 1L;
        Long userId = 1L;
        AuctionDetailsResponse auctionDetails = new AuctionDetailsResponse(1L, 2L, "닉네임2", "제품1", null, 1000,
                LocalDateTime.now().plusDays(1), PROCEEDING, false, 0L, false, 0L, 0);

        // when
        when(auctionRepository.findAuctionDetailsById(anyLong(), anyLong())).thenReturn(Optional.of(auctionDetails));

        // then
        assertDoesNotThrow(() -> {
            auctionService.getAuctionDetails(existingAuctionId, userId);
        });
    }

    @Test
    @DisplayName("경매 상세 조회 - 빈 값이 리턴 되는 경우 예외 발생")
    public void testGetAuctionDetails_NonExistentAuction() {
        // given
        Long nonExistentAuctionId = 999L;
        Long userId = 1L;

        // when
        when(auctionRepository.findAuctionDetailsById(anyLong(), anyLong())).thenReturn(Optional.empty());

        // then
        AuctionException auctionException = assertThrows(AuctionException.class, () -> {
            auctionService.getAuctionDetails(nonExistentAuctionId, userId);
        });
        assertThat(auctionException.getErrorCode()).isEqualTo(AUCTION_NOT_ACCESSIBLE);
    }

}
