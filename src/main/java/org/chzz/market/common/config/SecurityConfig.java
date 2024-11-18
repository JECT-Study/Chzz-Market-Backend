package org.chzz.market.common.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.error.handler.CustomAccessDeniedHandler;
import org.chzz.market.common.error.handler.CustomAuthenticationEntryPoint;
import org.chzz.market.common.error.handler.ExceptionHandlingFilter;
import org.chzz.market.common.filter.HttpCookieOAuth2AuthorizationRequestRepository;
import org.chzz.market.common.filter.JWTFilter;
import org.chzz.market.common.filter.NotFoundFilter;
import org.chzz.market.common.util.JWTUtil;
import org.chzz.market.domain.user.oauth2.CustomFailureHandler;
import org.chzz.market.domain.user.oauth2.CustomSuccessHandler;
import org.chzz.market.domain.user.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerMapping;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ACTUATOR = "/actuator/**";

    @Value("${client.url}")
    private String clientUrl;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final List<HandlerMapping> handlerMappings;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(ACTUATOR).permitAll()
                        .requestMatchers("/metrics").permitAll()
                        .requestMatchers("/api-docs", "/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
                        .requestMatchers(GET,
                                "/api/v1/auctions",
                                "/api/v1/auctions/{auctionId:\\d+}",
                                "/api/v1/auctions/{auctionId:\\d+}/simple",
                                "/api/v1/auctions/best",
                                "/api/v1/auctions/imminent",
                                "/api/v1/auctions/users/*",
                                "/api/v1/products",
                                "/api/v1/products/categories",
                                "/api/v1/products/{productId:\\d+}",
                                "/api/v1/products/users/*",
                                "/api/v1/notifications/subscribe",
                                "/api/v1/users/*",
                                "/api/v1/users/check/nickname/*").permitAll()
                        .requestMatchers(GET,
                                "/api/v2/auctions/categories",
                                "/api/v2/auctions/{auctionId:\\d+}").permitAll()
                        .requestMatchers(POST,
                                "/api/v1/users/tokens/reissue").permitAll()
                        .requestMatchers(POST, "/api/v1/users").hasRole("TEMP_USER")
                        .anyRequest().hasRole("USER")
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable).disable())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling((auth) -> auth.authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService)
                        )
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        .successHandler(customSuccessHandler)
                        .failureHandler(customFailureHandler)
                )
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new NotFoundFilter(handlerMappings), JWTFilter.class)
                .addFilterBefore(new ExceptionHandlingFilter(objectMapper), NotFoundFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList(clientUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
