package org.chzz.market.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.chzz.market.domain.user.dto.request.UpdateUserProfileRequest;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith({MockitoExtension.class})
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1, user2, user3;

    private UpdateUserProfileRequest updateUserProfileRequest;

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

        user3 = spy(User.builder()
                .id(3L)
                .nickname("닉네임 3")
                .bio("자기소개 3")
                .profileImageUrl("https://test")
                .build());

        updateUserProfileRequest = UpdateUserProfileRequest.builder()
                .nickname("수정된 닉네임")
                .bio("수정된 자기 소개")
                .objectKey("auction/image.jpg")
                .build();

        ReflectionTestUtils.setField(userService, "cloudfrontDomain", "https://cdn.example.com/test");
    }

    @Nested
    @DisplayName("사용자 회원가입 테스트")
    class CreateUserTest {
        @Test
        @DisplayName("1. 사용자 정보 업데이트가 성공하는 경우")
        public void createUser_Success() throws Exception {
            // given
            Long userId = 1L;
            UserCreateRequest userCreateRequest = new UserCreateRequest("bidderNickname", "bio");
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
            assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("2. UserRequest 에 bio와 link가 빈 문자열인 경우")
        public void createUser_WhenBioAndLinkAreEmptyStrings_ThenFieldsAreSetToNull() throws Exception {
            // given
            Long userId = 1L;
            UserCreateRequest userCreateRequest = new UserCreateRequest("newNickname", "");
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
            assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
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
    class UserProfileUpdateTest {

        @Test
        @DisplayName("1.성공 - 유저 프로필 수정")
        void updateUserProfile_Success() {
            // given
            when(userRepository.findById(any())).thenReturn(Optional.of(user1));
            when(userRepository.findByNickname(any())).thenReturn(Optional.empty());

            // when
            userService.updateUserProfile(user1.getId(),  updateUserProfileRequest);

            // then
            assertThat(user1.getNickname()).isEqualTo("수정된 닉네임");
            assertThat(user1.getBio()).isEqualTo("수정된 자기 소개");
        }

        @Test
        @DisplayName("2.성공 - 파일 포함 프로필 수정")
        void updateUserProfile_Success_WithFile() {
            // given
            when(userRepository.findById(any())).thenReturn(Optional.of(user1));
            when(userRepository.findByNickname(any())).thenReturn(Optional.empty());

//            when(imageService.uploadImage(file)).thenReturn("https://cdn.example.com/image.jpg");

            // when
            userService.updateUserProfile(user1.getId(), updateUserProfileRequest);

            // then
            assertThat(user1.getNickname()).isEqualTo("수정된 닉네임");
            assertThat(user1.getBio()).isEqualTo("수정된 자기 소개");
            assertThat(user1.getProfileImageUrl()).isEqualTo("https://cdn.example.com/test/auction/image.jpg");
        }

        @Test
        @DisplayName("3.성공 - 기존 이미지에서 기본 이미지로 변경")
        void updateUserProfile_WithExistingImage_SetToDefaultImage() {
            // given
            when(userRepository.findById(any())).thenReturn(Optional.of(user3));
            when(userRepository.findByNickname(any())).thenReturn(Optional.empty());

            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .nickname("수정된 닉네임")
                    .bio("수정된 자기 소개")
                    .useDefaultImage(true)
                    .build();

            // when
            userService.updateUserProfile(user3.getId(), request);

            // then
            assertThat(user3.getProfileImageUrl()).isNull(); // 기본 이미지로 변경 시 URL은 null
            assertThat(user3.getNickname()).isEqualTo("수정된 닉네임");
            assertThat(user3.getBio()).isEqualTo("수정된 자기 소개");
        }

        @Test
        @DisplayName("4.성공 - 기존 이미지에서 새 이미지 업로드")
        void updateUserProfile_WithExistingImage_UploadNewImage() {
            // given
            when(userRepository.findById(any())).thenReturn(Optional.of(user3));
            when(userRepository.findByNickname(any())).thenReturn(Optional.empty());

//            when(imageService.uploadImage(file)).thenReturn("https://cdn.example.com/new-image.jpg");

            UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                    .nickname("수정된 닉네임")
                    .bio("수정된 자기 소개")
                    .objectKey("new-image.jpg")
                    .build();

            // when
            userService.updateUserProfile(user3.getId(), request);

            // then
            assertThat(user3.getProfileImageUrl()).isEqualTo("https://cdn.example.com/test/new-image.jpg");
            assertThat(user3.getNickname()).isEqualTo("수정된 닉네임");
            assertThat(user3.getBio()).isEqualTo("수정된 자기 소개");
        }

        @Test
        @DisplayName("5.실패 - 유저를 찾을 수 없음")
        void updateUserProfile_Fail_UserNotFound() {
            // given
            when(userRepository.findById(any())).thenReturn(Optional.empty());

            // when, then
            assertThrows(UserException.class, () ->
                    userService.updateUserProfile(999L, updateUserProfileRequest)
            );
        }
    }
}
