package org.chzz.market.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.chzz.market.domain.bank_account.entity.BankAccount;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.entity.User.UserRole;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        when(userRepository.existsByNickname(userCreateRequest.getNickname())).thenReturn(false);
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
        UserCreateRequest userCreateRequest = new UserCreateRequest("nickname", BankAccount.BankName.KB, "1234567890",
                "", "");
        User user = User.builder()
                .email("test@gmail.com")
                .providerId("123456")
                .providerType(ProviderType.KAKAO)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname(userCreateRequest.getNickname())).thenReturn(false);

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
        when(userRepository.existsByNickname(userCreateRequest.getNickname())).thenReturn(true);

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
        String nickname = "availableNickname";

        when(userRepository.existsByNickname(nickname)).thenReturn(false);

        // when
        NicknameAvailabilityResponse response = userService.checkNickname(nickname);

        // then
        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("닉네임이 이미 사용 중인 경우")
    public void checkNickname_NotAvailable() throws Exception {
        // given
        String nickname = "unavailableNickname";

        when(userRepository.existsByNickname(nickname)).thenReturn(true);

        // when
        NicknameAvailabilityResponse response = userService.checkNickname(nickname);

        // then
        assertThat(response.isAvailable()).isFalse();
    }
}
