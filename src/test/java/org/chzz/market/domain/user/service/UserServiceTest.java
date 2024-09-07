package org.chzz.market.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.chzz.market.domain.bank_account.entity.BankAccount;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.UpdateProfileResponse;
import org.chzz.market.domain.user.dto.UpdateUserProfileRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private UpdateUserProfileRequest updateUserProfileRequest;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .nickname("오래된 닉네임")
                .id(1L)
                .bio("오래된 자기 소개")
                .link("오래된 URL")
                .build();

        updateUserProfileRequest = UpdateUserProfileRequest.builder()
                .nickname("수정된 닉네임")
                .bio("수정된 자기 소개")
                .link("수정된 URL")
                .build();

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Test
    @DisplayName("사용자 정보 업데이트가 성공하는 경우")
    public void createUser_Success() throws Exception {
        // given
        Long userId = 1L;
        UserCreateRequest userCreateRequest = new UserCreateRequest("nickname", BankAccount.BankName.KB, "1234567890",
                "bio", "http://link.com");
        User user = User.builder()
                .email("test@gmail.com")
                .providerId("123456")
                .providerType(ProviderType.KAKAO)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByNickname(userCreateRequest.getNickname())).thenReturn(Optional.empty());
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
    @DisplayName("UserRequest 에 bio와 link가 빈 문자열인 경우")
    public void createUser_WhenBioAndLinkAreEmptyStrings_ThenFieldsAreSetToNull() throws Exception {
        // given
        Long userId = 1L;
        UserCreateRequest userCreateRequest = new UserCreateRequest("newNickname", BankAccount.BankName.KB, "1234567890",
                "", "");
        User user = User.builder()
                .email("test@gmail.com")
                .providerId("123456")
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
    @DisplayName("사용자가 존재하지 않을 경우 예외 발생")
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
    @DisplayName("닉네임이 중복된 경우 예외 발생")
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
    @DisplayName("닉네임이 사용 가능한 경우")
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
    @DisplayName("닉네임이 이미 사용 중인 경우")
    public void checkNickname_NotAvailable() throws Exception {
        // given
        String unavailableNickname = "unavailableNickname";

        when(userRepository.findByNickname(unavailableNickname)).thenReturn(Optional.of(user1));

        // when
        NicknameAvailabilityResponse response = userService.checkNickname(unavailableNickname);

        // then
        assertThat(response.isAvailable()).isFalse();
    }

    @Nested
    @DisplayName("유저 프로필 수정")
    class userProfile_Update {
        @Test
        @DisplayName("유저 프로필 수정 성공")
        void updateUserProfile_Success() {
            // given
            when(userRepository.findByNickname("오래된 닉네임")).thenReturn(java.util.Optional.of(user1));
            when(userRepository.findByNickname("수정된 닉네임")).thenReturn(Optional.empty());

            // when
            UpdateProfileResponse response = userService.updateUserProfile("오래된 닉네임", user1.getId(), updateUserProfileRequest);

            // then
            assertNotNull(response);
            assertEquals("수정된 닉네임", response.nickname());
            assertEquals("수정된 자기 소개", response.bio());
            assertEquals("수정된 URL", response.url());

            assertEquals("수정된 닉네임", user1.getNickname());
            assertEquals("수정된 자기 소개", user1.getBio());
            assertEquals("수정된 URL", user1.getLink());

            verify(userRepository).findByNickname("오래된 닉네임");
            verify(userRepository).findByNickname("수정된 닉네임");
        }

        @Test
        @DisplayName("유저 프로필 수정 실패 - 유저를 찾을 수 없음")
        void updateUserProfile_Fail_UserNotFound() {
            // given
            when(userRepository.findByNickname("존재하지 않는 유저")).thenReturn(Optional.empty());

            // when, then
            assertThrows(UserException.class, () ->
                    userService.updateUserProfile("존재하지 않는 유저", user1.getId(), updateUserProfileRequest)
            );

            verify(userRepository).findByNickname("존재하지 않는 유저");
        }
    }
}