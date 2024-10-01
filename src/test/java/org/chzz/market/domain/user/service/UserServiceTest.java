package org.chzz.market.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.bank_account.entity.BankAccount;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.response.ParticipationCountsResponse;
import org.chzz.market.domain.user.dto.response.UpdateProfileResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.entity.User.UserRole;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserService userService;

    private User user1, user2;
    private Product product1, product2, product3, product4, product5, product6;
    private Product preRegisteredProduct1, preRegisteredProduct2, auctionProduct1, auctionProduct2;
    private Auction auction1, auction2, auction3, auction4, auction5, auction6, auction7, auction8;
    private Bid bid1, bid2, bid3, bid4, bid5, bid6;

    private UpdateUserProfileRequest updateUserProfileRequest;
    private ParticipationCountsResponse counts;

    @BeforeEach
    void setUp() {
        user1 = spy(User.builder()
                .id(1L)
                .nickname("닉네임 1")
                .bio("자기소개 1")
                .build());

        user2 = spy(User.builder()
                .id(2L)
                .nickname("닉네임 2")
                .bio("자기소개 2")
                .build());

        product1 = Product.builder().id(1L).name("제품1").user(user2).minPrice(1000).build();
        product2 = Product.builder().id(2L).name("제품2").user(user2).minPrice(2000).build();
        product3 = Product.builder().id(3L).name("제품3").user(user2).minPrice(3000).build();
        product4 = Product.builder().id(4L).name("제품4").user(user2).minPrice(4000).build();
        product5 = Product.builder().id(5L).name("제품5").user(user2).minPrice(5000).build();
        product6 = Product.builder().id(6L).name("제품6").user(user2).minPrice(6000).build();

        auction1 = Auction.builder().id(1L).product(product1).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction2 = Auction.builder().id(2L).product(product2).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(2)).build();
        auction3 = Auction.builder().id(3L).product(product3).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(3)).build();
        auction4 = Auction.builder().id(4L).product(product4).status(AuctionStatus.ENDED)
                .endDateTime(LocalDateTime.now().minusDays(1)).winnerId(user1.getId()).build();
        auction5 = Auction.builder().id(5L).product(product5).status(AuctionStatus.ENDED)
                .endDateTime(LocalDateTime.now().minusDays(2)).winnerId(user1.getId()).build();
        auction6 = Auction.builder().id(6L).product(product6).status(AuctionStatus.ENDED)
                .endDateTime(LocalDateTime.now().minusDays(3)).winnerId(user2.getId()).build();

        bid1 = Bid.builder().id(1L).auction(auction1).bidder(user1).amount(1500L).build();
        bid2 = Bid.builder().id(2L).auction(auction2).bidder(user1).amount(2500L).build();
        bid3 = Bid.builder().id(3L).auction(auction3).bidder(user1).amount(3500L).build();
        bid4 = Bid.builder().id(4L).auction(auction4).bidder(user1).amount(4500L).build();
        bid5 = Bid.builder().id(5L).auction(auction5).bidder(user1).amount(5500L).build();
        bid6 = Bid.builder().id(6L).auction(auction6).bidder(user1).amount(6500L).build();

        auction1.registerBid(bid1);
        auction2.registerBid(bid2);
        auction3.registerBid(bid3);
        auction4.registerBid(bid4);
        auction5.registerBid(bid5);
        auction6.registerBid(bid6);

        preRegisteredProduct1 = Product.builder().id(7L).name("사전등록상품1").user(user1).minPrice(7000).build();
        preRegisteredProduct2 = Product.builder().id(8L).name("사전등록상품2").user(user1).minPrice(8000).build();

        auctionProduct1 = Product.builder().id(9L).name("경매상품1").user(user1).minPrice(9000).build();
        auctionProduct2 = Product.builder().id(10L).name("경매상품2").user(user1).minPrice(10000).build();

        auction7 = Auction.builder().id(7L).product(auctionProduct1).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(4)).build();
        auction8 = Auction.builder().id(8L).product(auctionProduct2).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(5)).build();

        updateUserProfileRequest = UpdateUserProfileRequest.builder()
                .nickname("수정된 닉네임")
                .bio("수정된 자기 소개")
                .link("수정된 URL")
                .build();

        counts = new ParticipationCountsResponse(5L, 2L, 3L);

        List<Bid> user1Bids = Arrays.asList(bid1, bid2, bid3, bid4, bid5, bid6);
        ReflectionTestUtils.setField(user1, "bids", user1Bids);

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("사용자 회원가입 테스트")
    class CreateUserTest {
        @Test
        @DisplayName("1. 사용자 정보 업데이트가 성공하는 경우")
        public void createUser_Success() throws Exception {
            // given
            Long userId = 1L;
            UserCreateRequest userCreateRequest = new UserCreateRequest("nickname", "KB", "1234567890",
                    "bio", "http://link.com");
            User user = User.builder()
                    .email("test@gmail.com")
                    .providerId("123456")
                    .userRole(UserRole.TEMP_USER)
                    .providerType(ProviderType.KAKAO)
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            userRepository.findById(userId).ifPresent(System.out::println);
            when(userRepository.findByNickname(anyString())).thenReturn(Optional.empty());

            // when
            userService.completeUserRegistration(userId, userCreateRequest);
            // then
            assertThat(user.getNickname()).isEqualTo(userCreateRequest.getNickname());
            assertThat(user.getBio()).isEqualTo(userCreateRequest.getBio());
            assertThat(user.getLink()).isEqualTo(userCreateRequest.getLink());
            assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
            assertThat(user.getBankAccounts()).hasSize(1);
        }

        @Test
        @DisplayName("2. UserRequest 에 bio와 link가 빈 문자열인 경우")
        public void createUser_WhenBioAndLinkAreEmptyStrings_ThenFieldsAreSetToNull() throws Exception {
            // given
            Long userId = 1L;
            UserCreateRequest userCreateRequest = new UserCreateRequest("newNickname", "KB", "1234567890",
                    "", "");
            User user = User.builder()
                    .email("test@gmail.com")
                    .providerId("123456")
                    .userRole(UserRole.TEMP_USER)
                    .providerType(ProviderType.KAKAO)
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.findByNickname(userCreateRequest.getNickname())).thenReturn(Optional.empty());

            // when
            userService.completeUserRegistration(userId, userCreateRequest);

            // then
            assertThat(user.getNickname()).isEqualTo(userCreateRequest.getNickname());
            assertThat(user.getBio()).isNull();
            assertThat(user.getLink()).isNull();
            assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
            assertThat(user.getBankAccounts()).hasSize(1);
        }

        @Test
        @DisplayName("3. 사용자가 존재하지 않을 경우 예외 발생")
        public void createUser_UserNotFound() throws Exception {
            // given
            Long userId = 1L;
            UserCreateRequest userCreateRequest = mock(UserCreateRequest.class);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.completeUserRegistration(userId, userCreateRequest))
                    .isInstanceOf(UserException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("4. 닉네임이 중복된 경우 예외 발생")
        public void createUser_NicknameDuplication() throws Exception {
            // given
            Long userId = 1L;
            UserCreateRequest userCreateRequest = mock(UserCreateRequest.class);
            User user = mock(User.class);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.findByNickname(userCreateRequest.getNickname())).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userService.completeUserRegistration(userId, userCreateRequest))
                    .isInstanceOf(UserException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.NICKNAME_DUPLICATION);
        }

        @Test
        @DisplayName("5. 닉네임이 사용 가능한 경우")
        public void checkNickname_Available() throws Exception {
            // given
            String availableNickname = "availableNickname";

            when(userRepository.findByNickname(availableNickname)).thenReturn(Optional.empty());

            // when
            NicknameAvailabilityResponse response = userService.checkNickname(availableNickname);

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("6. 닉네임이 이미 사용 중인 경우")
        public void checkNickname_NotAvailable() throws Exception {
            // given
            String unavailableNickname = "unavailableNickname";

            when(userRepository.findByNickname(unavailableNickname)).thenReturn(Optional.of(user1));

            // when
            NicknameAvailabilityResponse response = userService.checkNickname(unavailableNickname);

            // then
            assertThat(response.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("유저 프로필 수정")
    class userProfile_Update {
        @Test
        @DisplayName("1. 유저 프로필 수정 성공")
        void updateUserProfile_Success() {
            // given
            when(userRepository.findById(any())).thenReturn(Optional.of(user1));
            when(userRepository.findByNickname(any())).thenReturn(Optional.empty());

            // when
            UpdateProfileResponse response = userService.updateUserProfile(user1.getId(), updateUserProfileRequest);

            // then
            assertNotNull(response);
            assertEquals("수정된 닉네임", response.nickname());
            assertEquals("수정된 자기 소개", response.bio());
            assertEquals("수정된 URL", response.url());

            assertEquals("수정된 닉네임", user1.getNickname());
            assertEquals("수정된 자기 소개", user1.getBio());
            assertEquals("수정된 URL", user1.getLink());
        }

        @Test
        @DisplayName("2. 유저 프로필 수정 실패 - 유저를 찾을 수 없음")
        void updateUserProfile_Fail_UserNotFound() {
            // given
            // when, then
            assertThrows(UserException.class, () ->
                    userService.updateUserProfile(999L, updateUserProfileRequest)
            );
        }
    }
}
