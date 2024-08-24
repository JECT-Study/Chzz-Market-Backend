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
    @Value("${oauth2.redirect-url}")
    private String oauth2RedirectUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String redirectUrl = oauth2RedirectUrl + "?status=failure";
        response.sendRedirect(redirectUrl);
        log.info("social login failure");
    }
}
