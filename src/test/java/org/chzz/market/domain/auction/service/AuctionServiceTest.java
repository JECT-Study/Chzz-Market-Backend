package org.chzz.market.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ALREADY_REGISTERED;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.auction.type.AuctionRegisterType.PRE_REGISTER;
import static org.chzz.market.domain.auction.type.AuctionRegisterType.REGISTER;
import static org.chzz.market.domain.auction.type.AuctionStatus.PROCEEDING;
import static org.chzz.market.domain.product.entity.Product.Category.ELECTRONICS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.PreRegisterRequest;
import org.chzz.market.domain.auction.dto.request.RegisterAuctionRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auction.dto.response.SimpleAuctionResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.auction.service.register.AuctionRegisterService;
import org.chzz.market.domain.auction.service.register.PreRegisterService;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductErrorCode;
import org.chzz.market.domain.product.error.ProductException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .productName("경매 등록 테스트 상품 이름")
                .description("경매 등록 테스트 상품 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .auctionRegisterType(REGISTER)
                .build();

        preRegisterRequest = PreRegisterRequest.builder()
                .productName("사전 등록 테스트 상품 이름")
                .description("사전 등록 테스트 상품 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .auctionRegisterType(PRE_REGISTER)
                .build();

        validStartAuctionRequest = StartAuctionRequest.builder()
                .productId(1L)
                .build();

        invalidStartAuctionRequest = StartAuctionRequest.builder()
                .productId(999L)
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
            savedProduct.addImages(createExistingImages(savedProduct));
            ReflectionTestUtils.setField(savedProduct, "id", productId);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // when
            RegisterResponse response = preRegisterService.register(userId, preRegisterRequest, images);

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
                preRegisterService.register(userId, invalidPreRegisterRequest, images);
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
            product.addImages(createExistingImages(product));
            ReflectionTestUtils.setField(product, "id", productId);

            Auction auction = AuctionTestFactory.createAuction(product, registerAuctionRequest, PROCEEDING);
            ReflectionTestUtils.setField(auction, "id", auctionId);

            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

            // when
            RegisterResponse response = auctionRegisterService.register(userId, registerAuctionRequest, images);

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
                auctionRegisterService.register(userId, invalidRegisterAuctionRequest, images);
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

            Auction newAuction = AuctionTestFactory.createAuction(preRegisteredProduct, registerAuctionRequest,
                    PROCEEDING);
            ReflectionTestUtils.setField(newAuction, "id", newAuctionId);
            ReflectionTestUtils.setField(newAuction, "endDateTime", LocalDateTime.now().plusHours(24));

            when(productRepository.findById(productId)).thenReturn(Optional.of(preRegisteredProduct));
            when(auctionRepository.existsByProductId(productId)).thenReturn(false);
            when(auctionRepository.save(any(Auction.class))).thenReturn(newAuction);

            // when
            StartAuctionResponse response = auctionService.startAuction(1L, validStartAuctionRequest);

            // then
            assertNotNull(response);
            assertEquals(newAuctionId, response.auctionId());
            assertEquals(productId, response.productId());
            assertEquals(PROCEEDING, response.status());
            assertTrue(response.endDateTime().isAfter(now) && response.endDateTime().isBefore(now.plusHours(25)));

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
            ProductException exception = assertThrows(ProductException.class,
                    () -> auctionService.startAuction(any(), invalidStartAuctionRequest));

            assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
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
                    () -> auctionService.startAuction(1L, validStartAuctionRequest));
            assertEquals(AUCTION_ALREADY_REGISTERED, exception.getErrorCode());

            verify(auctionRepository, never()).save(any(Auction.class));
        }
    }

    @Nested
    @DisplayName("경매 상세 조회 테스트")
    class GetAuctionDetailsTest {
        @Test
        @DisplayName("1. 값이 채워진 경우 예외 발생 안함")
        public void testGetAuctionDetails_ExistingAuction_NoException() {
            // given
            Long existingAuctionId = 1L;
            Long userId = 1L;
            AuctionDetailsResponse auctionDetails = new AuctionDetailsResponse(1L, "닉네임2", "null", "제품1", null, 1000,
                    ELECTRONICS, 123L, PROCEEDING, false, 0L, false, null, 0L, 0, false);

            // when
            when(auctionRepository.findAuctionDetailsById(anyLong(), anyLong())).thenReturn(
                    Optional.of(auctionDetails));

            // then
            assertDoesNotThrow(() -> {
                auctionService.getFullAuctionDetails(existingAuctionId, userId);
            });
        }

        @Test
        @DisplayName("2. 빈 값이 리턴 되는 경우 예외 발생")
        public void testGetAuctionDetails_NonExistentAuction() {
            // given
            Long nonExistentAuctionId = 999L;
            Long userId = 1L;

            // when
            when(auctionRepository.findAuctionDetailsById(anyLong(), anyLong())).thenReturn(Optional.empty());

            // then
            AuctionException auctionException = assertThrows(AuctionException.class, () -> {
                auctionService.getFullAuctionDetails(nonExistentAuctionId, userId);
            });
            assertThat(auctionException.getErrorCode()).isEqualTo(AUCTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("경매 간단 상세 조회 테스트")
    class GetSimpleAuctionDetailsTest {
        @Test
        @DisplayName("1. 판매자가 자신의 경매 상품을 조회할 때 성공")
        void getSimpleAuctionDetails_Success() {
            // given
            Long auctionId = 1L;
            SimpleAuctionResponse response = new SimpleAuctionResponse("image1.jpg", "Product 1", 10000, 5L);

            when(auctionRepository.findSimpleAuctionDetailsById(auctionId)).thenReturn(Optional.of(response));

            // when
            SimpleAuctionResponse result = auctionService.getSimpleAuctionDetails(auctionId);

            // then
            assertNotNull(result);
            assertEquals("image1.jpg", result.imageUrl());
            assertEquals("Product 1", result.productName());
            assertEquals(10000, result.minPrice());
            assertEquals(5L, result.participantCount());

            verify(auctionRepository).findSimpleAuctionDetailsById(auctionId);
        }

        @Test
        @DisplayName("2. 판매자가 아닌 사용자가 경매 상품을 조회할 때 예외 발생")
        void getSimpleAuctionDetails_NotAccessible() {
            // given
            Long auctionId = 1L;
            Auction auction = mock(Auction.class);
            Product product = mock(Product.class);

            // when & then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.getSimpleAuctionDetails(auctionId));
            assertEquals(AUCTION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("3. 존재하지 않는 경매 상품을 조회할 때 예외 발생")
        void getSimpleAuctionDetails_NotFound() {
            // given
            Long nonExistentAuctionId = 999L;

            // when & then
            AuctionException exception = assertThrows(AuctionException.class,
                    () -> auctionService.getSimpleAuctionDetails(nonExistentAuctionId));
            assertEquals(AUCTION_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("내가 성공한 경매 조회 테스트")
    class GetWonAuctionHistoryTest {
        @Test
        @DisplayName("1. 유효한 요청으로 낙찰된 경매 조회 성공")
        void getWonAuctionHistory_Success() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endDateTime"));

            List<WonAuctionResponse> wonAuctions = List.of(
                    new WonAuctionResponse(1L, "Product 1", "image1.jpg", 10000, 3L, LocalDateTime.now(), 15000L),
                    new WonAuctionResponse(2L, "Product 2", "image2.jpg", 20000, 3L, LocalDateTime.now(), 25000L)
            );

            Page<WonAuctionResponse> mockPage = new PageImpl<>(wonAuctions, pageable, wonAuctions.size());

            when(auctionRepository.findWonAuctionHistoryByUserId(userId, pageable)).thenReturn(mockPage);

            // when
            Page<WonAuctionResponse> resultPage = auctionService.getWonAuctionHistory(userId, pageable);

            // then
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.getContent()).hasSize(2);
            assertThat(resultPage.getContent().get(0).auctionId()).isEqualTo(1L);
            assertThat(resultPage.getContent().get(0).productName()).isEqualTo("Product 1");
            assertThat(resultPage.getContent().get(1).auctionId()).isEqualTo(2L);
            assertThat(resultPage.getContent().get(1).productName()).isEqualTo("Product 2");

            verify(auctionRepository, times(1)).findWonAuctionHistoryByUserId(userId, pageable);
        }

        @Test
        @DisplayName("2. 낙찰된 경매가 없는 경우 빈 목록 반환")
        void getWonAuctionHistory_EmptyList() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endDateTime"));
            Page<WonAuctionResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(auctionRepository.findWonAuctionHistoryByUserId(userId, pageable)).thenReturn(emptyPage);

            // when
            Page<WonAuctionResponse> resultPage = auctionService.getWonAuctionHistory(userId, pageable);

            // then
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.getContent()).isEmpty();
            assertThat(resultPage.getTotalElements()).isZero();

            verify(auctionRepository, times(1)).findWonAuctionHistoryByUserId(userId, pageable);
        }

        @Test
        @DisplayName("3. 페이지네이션 동작 확인")
        void getWonAuctionHistory_Pagination() {
            // given
            Long userId = 1L;
            Pageable firstPageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "endDateTime"));
            Pageable secondPageable = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "endDateTime"));

            List<WonAuctionResponse> allAuctions = List.of(
                    new WonAuctionResponse(1L, "Product 1", "image1.jpg", 10000, 3L, LocalDateTime.now(), 15000L),
                    new WonAuctionResponse(2L, "Product 2", "image2.jpg", 20000, 3L, LocalDateTime.now(), 25000L)
            );

            Page<WonAuctionResponse> firstPage = new PageImpl<>(allAuctions.subList(0, 1), firstPageable,
                    allAuctions.size());
            Page<WonAuctionResponse> secondPage = new PageImpl<>(allAuctions.subList(1, 2), secondPageable,
                    allAuctions.size());

            when(auctionRepository.findWonAuctionHistoryByUserId(userId, firstPageable)).thenReturn(firstPage);
            when(auctionRepository.findWonAuctionHistoryByUserId(userId, secondPageable)).thenReturn(secondPage);

            // when
            Page<WonAuctionResponse> firstResultPage = auctionService.getWonAuctionHistory(userId, firstPageable);
            Page<WonAuctionResponse> secondResultPage = auctionService.getWonAuctionHistory(userId, secondPageable);

            // then
            assertThat(firstResultPage.getContent()).hasSize(1);
            assertThat(firstResultPage.getContent().get(0).auctionId()).isEqualTo(1L);
            assertThat(secondResultPage.getContent()).hasSize(1);
            assertThat(secondResultPage.getContent().get(0).auctionId()).isEqualTo(2L);

            verify(auctionRepository, times(1)).findWonAuctionHistoryByUserId(userId, firstPageable);
            verify(auctionRepository, times(1)).findWonAuctionHistoryByUserId(userId, secondPageable);
        }

        @Test
        @DisplayName("4. 정렬 순서 확인 (경매 종료 시간 내림차순)")
        void getWonAuctionHistory_SortOrder() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endDateTime"));

            LocalDateTime now = LocalDateTime.now();
            List<WonAuctionResponse> wonAuctions = List.of(
                    new WonAuctionResponse(1L, "Product 1", "image1.jpg", 10000, 3L, now, 15000L),
                    new WonAuctionResponse(2L, "Product 2", "image2.jpg", 20000, 3L, now.minusHours(1), 25000L),
                    new WonAuctionResponse(3L, "Product 3", "image3.jpg", 30000, 3L, now.minusHours(2), 35000L)
            );

            Page<WonAuctionResponse> mockPage = new PageImpl<>(wonAuctions, pageable, wonAuctions.size());

            when(auctionRepository.findWonAuctionHistoryByUserId(userId, pageable)).thenReturn(mockPage);

            // when
            Page<WonAuctionResponse> resultPage = auctionService.getWonAuctionHistory(userId, pageable);

            // then
            assertThat(resultPage.getContent()).hasSize(3);
            assertThat(resultPage.getContent()).isSortedAccordingTo(
                    Comparator.comparing(WonAuctionResponse::endDateTime).reversed()
            );

            verify(auctionRepository, times(1)).findWonAuctionHistoryByUserId(userId, pageable);
        }
    }

    @Nested
    @DisplayName("내가 실패한 경매 조회 테스트")
    class GetLostAuctionHistoryTest {
        @Test
        @DisplayName("1. 유효한 요청으로 낙찰하지 못한 경매 조회 성공")
        void getLostAuctionHistory_Success() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endDateTime"));

            List<LostAuctionResponse> LostAuctions = List.of(
                    new LostAuctionResponse(1L, "Product 1", "image1.jpg", 10000, 3L, LocalDateTime.now(), 15000L),
                    new LostAuctionResponse(2L, "Product 2", "image2.jpg", 20000, 3L, LocalDateTime.now(), 25000L)
            );

            Page<LostAuctionResponse> mockPage = new PageImpl<>(LostAuctions, pageable, LostAuctions.size());

            when(auctionRepository.findLostAuctionHistoryByUserId(userId, pageable)).thenReturn(mockPage);

            // when
            Page<LostAuctionResponse> resultPage = auctionService.getLostAuctionHistory(userId, pageable);

            // then
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.getContent()).hasSize(2);
            assertThat(resultPage.getContent().get(0).auctionId()).isEqualTo(1L);
            assertThat(resultPage.getContent().get(0).productName()).isEqualTo("Product 1");
            assertThat(resultPage.getContent().get(1).auctionId()).isEqualTo(2L);
            assertThat(resultPage.getContent().get(1).productName()).isEqualTo("Product 2");

            verify(auctionRepository, times(1)).findLostAuctionHistoryByUserId(userId, pageable);
        }

        @Test
        @DisplayName("2. 낙찰하지 못한 경매가 없는 경우 빈 목록 반환")
        void getLostAuctionHistory_EmptyList() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endDateTime"));
            Page<LostAuctionResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(auctionRepository.findLostAuctionHistoryByUserId(userId, pageable)).thenReturn(emptyPage);

            // when
            Page<LostAuctionResponse> resultPage = auctionService.getLostAuctionHistory(userId, pageable);

            // then
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.getContent()).isEmpty();
            assertThat(resultPage.getTotalElements()).isZero();

            verify(auctionRepository, times(1)).findLostAuctionHistoryByUserId(userId, pageable);
        }

        @Test
        @DisplayName("3. 페이지네이션 동작 확인")
        void getLostAuctionHistory_Pagination() {
            // given
            Long userId = 1L;
            Pageable firstPageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "endDateTime"));
            Pageable secondPageable = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "endDateTime"));

            List<LostAuctionResponse> allAuctions = List.of(
                    new LostAuctionResponse(1L, "Product 1", "image1.jpg", 10000, 3L, LocalDateTime.now(), 15000L),
                    new LostAuctionResponse(2L, "Product 2", "image2.jpg", 20000, 3L, LocalDateTime.now(), 25000L)
            );

            Page<LostAuctionResponse> firstPage = new PageImpl<>(allAuctions.subList(0, 1), firstPageable,
                    allAuctions.size());
            Page<LostAuctionResponse> secondPage = new PageImpl<>(allAuctions.subList(1, 2), secondPageable,
                    allAuctions.size());

            when(auctionRepository.findLostAuctionHistoryByUserId(userId, firstPageable)).thenReturn(firstPage);
            when(auctionRepository.findLostAuctionHistoryByUserId(userId, secondPageable)).thenReturn(secondPage);

            // when
            Page<LostAuctionResponse> firstResultPage = auctionService.getLostAuctionHistory(userId, firstPageable);
            Page<LostAuctionResponse> secondResultPage = auctionService.getLostAuctionHistory(userId, secondPageable);

            // then
            assertThat(firstResultPage.getContent()).hasSize(1);
            assertThat(firstResultPage.getContent().get(0).auctionId()).isEqualTo(1L);
            assertThat(secondResultPage.getContent()).hasSize(1);
            assertThat(secondResultPage.getContent().get(0).auctionId()).isEqualTo(2L);

            verify(auctionRepository, times(1)).findLostAuctionHistoryByUserId(userId, firstPageable);
            verify(auctionRepository, times(1)).findLostAuctionHistoryByUserId(userId, secondPageable);
        }

        @Test
        @DisplayName("4. 정렬 순서 확인 (경매 종료 시간 내림차순)")
        void getLostAuctionHistory_SortOrder() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endDateTime"));

            LocalDateTime now = LocalDateTime.now();
            List<LostAuctionResponse> lostAuctions = List.of(
                    new LostAuctionResponse(1L, "Product 1", "image1.jpg", 10000, 3L, now, 15000L),
                    new LostAuctionResponse(2L, "Product 2", "image2.jpg", 20000, 3L, now.minusHours(1), 25000L),
                    new LostAuctionResponse(3L, "Product 3", "image3.jpg", 30000, 3L, now.minusHours(2), 35000L)
            );

            Page<LostAuctionResponse> mockPage = new PageImpl<>(lostAuctions, pageable, lostAuctions.size());

            when(auctionRepository.findLostAuctionHistoryByUserId(userId, pageable)).thenReturn(mockPage);

            // when
            Page<LostAuctionResponse> resultPage = auctionService.getLostAuctionHistory(userId, pageable);

            // then
            assertThat(resultPage.getContent()).hasSize(3);
            assertThat(resultPage.getContent()).isSortedAccordingTo(
                    Comparator.comparing(LostAuctionResponse::endDateTime).reversed()
            );

            verify(auctionRepository, times(1)).findLostAuctionHistoryByUserId(userId, pageable);
        }

        @Test
        @DisplayName("5. 최고 입찰가 확인")
        void getLostAuctionHistory_HighestBid() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "endDateTime"));

            List<LostAuctionResponse> lostAuctions = List.of(
                    new LostAuctionResponse(1L, "Product 1", "image1.jpg", 10000, 3L, LocalDateTime.now(), 15000L),
                    new LostAuctionResponse(2L, "Product 2", "image2.jpg", 20000, 3L, LocalDateTime.now(), 25000L)
            );

            Page<LostAuctionResponse> mockPage = new PageImpl<>(lostAuctions, pageable, lostAuctions.size());

            when(auctionRepository.findLostAuctionHistoryByUserId(userId, pageable)).thenReturn(mockPage);

            // when
            Page<LostAuctionResponse> resultPage = auctionService.getLostAuctionHistory(userId, pageable);

            // then
            assertThat(resultPage.getContent()).hasSize(2);
            assertThat(resultPage.getContent().get(0).bidAmount()).isEqualTo(15000L);
            assertThat(resultPage.getContent().get(1).bidAmount()).isEqualTo(25000L);

            verify(auctionRepository, times(1)).findLostAuctionHistoryByUserId(userId, pageable);
        }
    }

    @Nested
    @DisplayName("낙찰 정보 조회 테스트")
    class GetWinningBidTest {
        @Test
        @DisplayName("정상적으로 낙찰 정보를 조회한다.")
        public void getWinningBidByAuctionId_Success() throws Exception {
            Product product = Product.builder()
                    .user(User.builder().id(user.getId() + 1).build())
                    .build();

            //given
            Auction auction = Auction.builder()
                    .id(1L)
                    .product(product)
                    .winnerId(user.getId())
                    .build();

            //when
            when(auctionRepository.findById(auction.getId())).thenReturn(Optional.of(auction));
            when(auctionRepository.findWinningBidById(auction.getId())).thenReturn(
                    Optional.of(mock(WonAuctionDetailsResponse.class)));

            //then
            assertDoesNotThrow(() -> auctionService.getWinningBidByAuctionId(user.getId(), auction.getId()));
        }

        @Test
        @DisplayName("사용자가 낙찰자가 아니면 AuctionException 발생")
        void getWinningBidByAuctionId_NotWinner() {
            // given
            Auction auction = Auction.builder()
                    .id(1L)
                    .winnerId(user.getId() + 1) // user가 낙찰자가 될 수 없음
                    .build();

            when(auctionRepository.findById(auction.getId())).thenReturn(Optional.of(auction));

            // then
            assertThatThrownBy(() -> auctionService.getWinningBidByAuctionId(user.getId(), auction.getId()))
                    .isInstanceOf(AuctionException.class);
        }
    }

    private List<MultipartFile> createMockMultipartFiles() {
        MultipartFile mockFile1 = new MockMultipartFile(
                "testImage1.jpg", "testImage1.jpg", "image/jpeg", "test image content 1".getBytes());
        MultipartFile mockFile2 = new MockMultipartFile(
                "testImage2.jpg", "testImage2.jpg", "image/jpeg", "test image content 2".getBytes());
        return List.of(mockFile1, mockFile2);
    }

    private List<Image> createExistingImages(Product product) {
        return List.of(
                new Image(1L, "existingImage1.jpg", 1, product),
                new Image(2L, "existingImage2.jpg", 2, product)
        );
    }
}
