package org.chzz.market.domain.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chzz.market.domain.oauth.dto.NaverUserInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NaverOAuth2UserService{
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    String clientId;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    String redirectUri;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    String clientSecret;
    @Value("${spring.security.oauth2.client.registration.naver.authorization-grant-type}")
    String authorizationGrantType;

    // NAVER 액세스 토큰 발급
    public String getAccessToken(String code) throws JsonProcessingException {

        // 1. header 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        // 2. body 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", authorizationGrantType); //고정값
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        // 3. header + body
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, httpHeaders);

        // 4. http 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // 5. HTTP 응답 (JSON) 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // 6. 카카오 액세스 토큰 반환
        return jsonNode.get("access_token").asText();
    }

    // NAVER 유저 정보 조회
    public NaverUserInfoResponse getNaverUserInfo(String accessToken) throws JsonProcessingException {
        // 1. HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 2. HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.POST,
                naverUserInfoRequest,
                String.class
        );

        // 3. responseBody에 있는 정보 꺼내기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Map<String, Object> attributes = objectMapper.convertValue(jsonNode.get("response"), Map.class);

        return new NaverUserInfoResponse(attributes);
    }
}