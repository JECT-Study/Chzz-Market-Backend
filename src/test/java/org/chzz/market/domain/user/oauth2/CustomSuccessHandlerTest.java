package org.chzz.market.domain.user.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import org.chzz.market.domain.token.service.TokenService;
import org.chzz.market.domain.user.dto.CustomUserDetails;
import org.chzz.market.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

class CustomSuccessHandlerTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomSuccessHandler customSuccessHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private CustomUserDetails customUserDetails;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        user = mock(User.class);
        customUserDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUser()).thenReturn(user);
    }

    private void setPrivateField(Object targetObject, String fieldName, String value) throws Exception {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, value);
    }

    @Test
    @DisplayName("임시 유저일 때 추가 정보 입력 페이지로 리다이렉트하고 임시 토큰 발급")
    void onAuthenticationSuccess_WhenTempUser_RedirectToAdditionalInfoAndCreateTempToken() throws IOException {
        // given
        when(user.isTempUser()).thenReturn(true);
        when(tokenService.createTempToken(user)).thenReturn("temp-token");

        // when
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getCookies()).hasSize(1);
        assertThat(response.getCookies()[0].getValue()).isEqualTo("temp-token");
        verify(tokenService, times(1)).createTempToken(user);
    }

    @Test
    @DisplayName("일반 유저일 때 메인 페이지로 리다이렉트하고 리프레시 토큰 발급")
    void onAuthenticationSuccess_WhenRegularUser_RedirectToMainAndCreateRefreshToken() throws IOException {
        // given
        when(user.isTempUser()).thenReturn(false);
        when(tokenService.createRefreshToken(user)).thenReturn("refresh-token");

        // when
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getCookies()).hasSize(1);
        assertThat(response.getCookies()[0].getValue()).isEqualTo("refresh-token");
        verify(tokenService, times(1)).createRefreshToken(user);
    }
}
