package org.chzz.market.domain.user.service;

import org.chzz.market.domain.user.dto.UpdateProfileResponse;
import org.chzz.market.domain.user.dto.UpdateUserProfileRequest;
import org.chzz.market.domain.user.entity.User;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UpdateUserProfileRequest updateUserProfileRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .nickname("오래된 닉네임")
                .description("오래된 자기 소개")
                .region("오래된 지역")
                .link("오래된 URL")
                .build();

        updateUserProfileRequest = UpdateUserProfileRequest.builder()
                .nickname("수정된 닉네임")
                .bio("수정된 자기 소개")
                .region("수정된 지역")
                .link("수정된 URL")
                .build();

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("유저 프로필 수정")
    class userProfile_Update {
        @Test
        @DisplayName("유저 프로필 수정 성공")
        void updateUserProfile_Success() {
            // given
            when(userRepository.findByNickname("오래된 닉네임")).thenReturn(java.util.Optional.of(user));

            // when
            UpdateProfileResponse response = userService.updateUserProfile("오래된 닉네임", updateUserProfileRequest);

            // then
            assertNotNull(response);
            assertEquals(response.nickname(), updateUserProfileRequest.getNickname());
            assertEquals(response.description(), updateUserProfileRequest.getBio());
            assertEquals(response.region(), updateUserProfileRequest.getRegion());
            assertEquals(response.url(), updateUserProfileRequest.getLink());

            // verify
            verify(userRepository).findByNickname("오래된 닉네임");

            assertEquals("수정된 닉네임", user.getNickname());
            assertEquals("수정된 자기 소개", user.getDescription());
            assertEquals("수정된 지역", user.getRegion());
            assertEquals("수정된 URL", user.getLink());
        }

        @Test
        @DisplayName("유저 프로필 수정 실패 - 유저를 찾을 수 없음")
        void updateUserProfile_Fail_UserNotFound() {
            // given
            when(userRepository.findByNickname("존재하지 않는 유저")).thenReturn(Optional.empty());

            // when, then
            assertThrows(UserException.class, () ->
                    userService.updateUserProfile("존재하지 않는 유저", updateUserProfileRequest)
            );

            verify(userRepository).findByNickname("존재하지 않는 유저");
        }
    }


}