package org.chzz.market.common.filter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.dto.ApiEndpoint;
import org.chzz.market.common.util.CookieUtil;
import org.chzz.market.common.util.JWTUtil;
import org.chzz.market.domain.token.entity.TokenType;
import org.chzz.market.domain.user.dto.CustomUserDetails;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final ApiEndpoint SIGN_UP_ENDPOINT = new ApiEndpoint("/api/v1/users", POST);
    private static final List<ApiEndpoint> EXCLUDED_ENDPOINTS = List.of(
            new ApiEndpoint("/api/v1/auctions/best", GET),
            new ApiEndpoint("/api/v1/auctions/imminent", GET),
            new ApiEndpoint("/api/v1/users/tokens/reissue", POST)
    );

    private final JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return EXCLUDED_ENDPOINTS.stream().anyMatch(endpoint -> endpoint.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        Cookie tempCookie = CookieUtil.getCookieByName(request, TokenType.TEMP.name());

        if (authorization != null && authorization.startsWith(BEARER_TOKEN_PREFIX)) {
            String accessToken = authorization.substring(BEARER_TOKEN_PREFIX.length());
            jwtUtil.validateToken(accessToken, TokenType.ACCESS);
            setAuthentication(accessToken);
            filterChain.doFilter(request, response);
        } else if (isSignUpRequest(request) && tempCookie != null) {
            String tempToken = tempCookie.getValue();
            jwtUtil.validateToken(tempToken, TokenType.TEMP);
            setAuthentication(tempToken);
            filterChain.doFilter(request, response);
        } else {
            filterChain.doFilter(request, response);
            return;
        }
    }

    private void setAuthentication(String token) {
        Long id = jwtUtil.getId(token);
        String role = jwtUtil.getRole(token);
        User user = User.builder()
                .id(id)
                .userRole(UserRole.valueOf(role))
                .build();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
                customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private boolean isSignUpRequest(HttpServletRequest request) {
        return SIGN_UP_ENDPOINT.matches(request);
    }
}
