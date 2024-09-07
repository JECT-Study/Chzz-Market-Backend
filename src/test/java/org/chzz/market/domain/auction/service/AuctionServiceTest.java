package org.chzz.market.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.auction.entity.Auction.AuctionStatus.*;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.*;
import static org.chzz.market.domain.product.entity.Product.Category.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.chzz.market.domain.auction.enums.AuctionRegisterType.PRE_REGISTER;
import static org.chzz.market.domain.auction.enums.AuctionRegisterType.REGISTER;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;

import org.chzz.market.domain.auction.dto.request.PreRegisterRequest;
import org.chzz.market.domain.auction.dto.request.RegisterAuctionRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;

import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.auction.service.register.AuctionRegisterService;
import org.chzz.market.domain.auction.service.register.PreRegisterService;
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
    @InjectMocks
    private PreRegisterService preRegisterService;
    @InjectMocks
    private AuctionRegisterService auctionRegisterService;

    private ProductTestFactory productTestFactory;
    private AuctionTestFactory auctionTestFactory;
    private UserTestFactory userTestFactory;

    private User user;
    private BaseRegisterRequest registerAuctionRequest, preRegisterRequest;
    private StartAuctionRequest validStartAuctionRequest, invalidStartAuctionRequest;

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

        registerAuctionRequest = RegisterAuctionRequest.builder()
                .userId(1L)
                .productName("경매 등록 테스트 상품 이름")
                .description("경매 등록 테스트 상품 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .auctionRegisterType(REGISTER)
                .build();

        preRegisterRequest = PreRegisterRequest.builder()
                .userId(1L)
                .productName("사전 등록 테스트 상품 이름")
                .description("사전 등록 테스트 상품 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .auctionRegisterType(PRE_REGISTER)
                .build();

        validStartAuctionRequest = StartAuctionRequest.builder()
                .productId(1L)
                .userId(1L)
                .build();

        invalidStartAuctionRequest = StartAuctionRequest.builder()
                .productId(999L)
                .userId(1L)
                .build();

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("상품 사전 등록 테스트")
    class PreRegisterTest {

        @Test
        @DisplayName("1. 유효한 요청으로 상품 사전 등록 성공 응답")
        void preRegister_Success() {
            // given
            Long userId = 1L;
            Long productId = 1L;

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(imageService.uploadImages(anyList())).thenReturn(List.of("image1.jpg", "image2.jpg"));

            List<MultipartFile> images = createMockMultipartFiles();

            Product savedProduct = ProductTestFactory.createProduct(preRegisterRequest, user);
            ReflectionTestUtils.setField(savedProduct, "id", productId);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // when
            RegisterResponse response = preRegisterService.register(preRegisterRequest, images);

            // then
            assertNotNull(response);
            assertEquals(productId, response.getProductId());
            verify(userRepository, times(1)).findById(userId);
            verify(productRepository, times(1)).save(any(Product.class));
            verify(imageService, times(1)).uploadImages(anyList());
        }

        @Test
        @DisplayName("2. 존재하지 않는 사용자로 상품 사전 등록 실패")
        void preRegister_UserNotFound() {
            // Given
            Long userId = 999L;
            PreRegisterRequest invalidPreRegisterRequest = PreRegisterRequest.builder()
                    .userId(userId)
                    .productName("사전 등록 테스트 상품 이름")
                    .description("사전 등록 테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .auctionRegisterType(PRE_REGISTER)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            List<MultipartFile> images = createMockMultipartFiles();

            // When & Then
            assertThrows(UserException.class, () -> {
                preRegisterService.register(invalidPreRegisterRequest, images);
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
            Long userId = 1L;
            Long productId = 1L;
            Long auctionId = 1L;

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(imageService.uploadImages(anyList())).thenReturn(List.of("image1.jpg", "image2.jpg"));

            List<MultipartFile> images = createMockMultipartFiles();

            Product product = ProductTestFactory.createProduct(registerAuctionRequest, user);
            ReflectionTestUtils.setField(product, "id", productId);

            Auction auction = AuctionTestFactory.createAuction(product, registerAuctionRequest, PROCEEDING);
            ReflectionTestUtils.setField(auction, "id", auctionId);

            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

            // when
            RegisterResponse response = auctionRegisterService.register(registerAuctionRequest, images);

            // then
            assertNotNull(response);
            assertEquals(productId, response.getProductId());
            verify(userRepository, times(1)).findById(userId);
            verify(productRepository, times(1)).save(any(Product.class));
            verify(auctionRepository, times(1)).save(any(Auction.class));
            verify(imageService, times(1)).uploadImages(anyList());
        }

        @Test
        @DisplayName("2. 존재하지 않는 사용자로 상품 경매 등록 실패")
        void registerAuction_UserNotFound() {
            // Given
            Long userId = 999L;
            RegisterAuctionRequest invalidRegisterAuctionRequest = RegisterAuctionRequest.builder()
                    .userId(userId)
                    .productName("경매 등록 테스트 상품 이름")
                    .description("경매 등록 테스트 상품 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .auctionRegisterType(REGISTER)
                    .build();

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            List<MultipartFile> images = createMockMultipartFiles();

            // When & Then
            assertThrows(UserException.class, () -> {
                auctionRegisterService.register(invalidRegisterAuctionRequest, images);
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
            Long productId = 1L;
            Long newAuctionId = 2L;
            LocalDateTime now = LocalDateTime.now();

            Product preRegisteredProduct = ProductTestFactory.createProduct(preRegisterRequest, user);
            ReflectionTestUtils.setField(preRegisteredProduct, "id", productId);

            Auction newAuction = AuctionTestFactory.createAuction(preRegisteredProduct, registerAuctionRequest, PROCEEDING);
            ReflectionTestUtils.setField(newAuction, "id", newAuctionId);
            ReflectionTestUtils.setField(newAuction, "endDateTime", LocalDateTime.now().plusHours(24));

            when(productRepository.findById(productId)).thenReturn(Optional.of(preRegisteredProduct));
            when(auctionRepository.existsByProductId(productId)).thenReturn(false);
            when(auctionRepository.save(any(Auction.class))).thenReturn(newAuction);

            // when
            StartAuctionResponse response = auctionService.startAuction(validStartAuctionRequest);

            // then
            assertNotNull(response);
            assertEquals(newAuctionId, response.auctionId());
            assertEquals(productId, response.productId());
            assertEquals(PROCEEDING, response.status());
            assertTrue(response.endTime().isAfter(now) && response.endTime().isBefore(now.plusHours(25)));

            verify(productRepository).findById(productId);
            verify(auctionRepository).existsByProductId(productId);
            verify(auctionRepository).save(any(Auction.class));
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품 ID로 전환 시도 실패")
        void startAuction_NotFound() {
            // Given
            Long nonExistentProductId = 999L;
            when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

            // When & Then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.startAuction(invalidStartAuctionRequest));

            assertEquals(AUCTION_NOT_FOUND, exception.getErrorCode());
            verify(auctionRepository, never()).save(any(Auction.class));
        }

        @Test
        @DisplayName("3. 이미 등록된 경매 상품 전환 시도 실패")
        void startAuction_AlreadyProceeding() {
            // given
            Long productId = 1L;

            Product product = ProductTestFactory.createProduct(preRegisterRequest, user);
            ReflectionTestUtils.setField(product, "id", productId);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(auctionRepository.existsByProductId(productId)).thenReturn(true);

            // When & Then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.startAuction(validStartAuctionRequest));
            assertEquals(AUCTION_ALREADY_REGISTERED, exception.getErrorCode());

            verify(auctionRepository, never()).save(any(Auction.class));
        }
    }

    @Test
    @DisplayName("경매 상세 조회 - 값이 채워진 경우 예외 발생 안함")
    public void testGetAuctionDetails_ExistingAuction_NoException() {
        // given
        Long existingAuctionId = 1L;
        Long userId = 1L;
        AuctionDetailsResponse auctionDetails = new AuctionDetailsResponse(1L, "닉네임2", "제품1", null, 1000,
                LocalDateTime.now().plusDays(1), PROCEEDING, false, 0L, false, null, 0L, 0);

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

    private List<MultipartFile> createMockMultipartFiles() {
        MultipartFile mockFile1 = new MockMultipartFile(
                "testImage1.jpg", "testImage1.jpg", "image/jpeg", "test image content 1".getBytes());
        MultipartFile mockFile2 = new MockMultipartFile(
                "testImage2.jpg", "testImage2.jpg", "image/jpeg", "test image content 2".getBytes());
        return List.of(mockFile1, mockFile2);
    }
}
