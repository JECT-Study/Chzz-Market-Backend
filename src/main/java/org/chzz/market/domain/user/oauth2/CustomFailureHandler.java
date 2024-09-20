package org.chzz.market.domain.user.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Slf4j
@Configuration
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private static final String REDIRECT_URL_FAILURE = "/login?status=failure";

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.sendRedirect(clientUrl + REDIRECT_URL_FAILURE);
        log.error("social login failure");
    }
}
