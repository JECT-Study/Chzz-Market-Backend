package org.chzz.market.domain.auction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.domain.auction.dto.RegisterAuctionRequest;
import org.chzz.market.domain.auction.dto.RegisterAuctionResponse;
import org.chzz.market.domain.auction.dto.StartAuctionResponse;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.auction.entity.Auction.AuctionStatus.*;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.*;
import static org.chzz.market.domain.product.entity.Product.Category.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AWSConfig.class)
public class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile image1, image2, image3;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

        image1 = new MockMultipartFile("images", "image1.jpg", "image/jpg", "image1".getBytes());
        image2 = new MockMultipartFile("images", "image2.jpg", "image/jpg", "image2".getBytes());
        image3 = new MockMultipartFile("images", "image3.jpg", "image/jpg", "image3".getBytes());
    }

    @Nested
    @DisplayName("상품 경매 등록 테스트")
    class RegisterAuctionTest {

        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("1. 유효한 요청으로 경매 상품 등록 성공 응답")
        void registerAuction_Success() throws Exception {
            RegisterAuctionRequest validRequest = RegisterAuctionRequest.builder()
                    .userId(1L)
                    .productName("테스트 상품")
                    .description("테스트 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PROCEEDING)
                    .build();

            RegisterAuctionResponse response = new RegisterAuctionResponse(1L, 1L, PROCEEDING, "success");
            when(auctionService.register(any(RegisterAuctionRequest.class))).thenReturn(response);

            mockMvc.perform(multipart("/api/v1/auctions/register")
                    .file(image1).file(image2).file(image3)
                    .param("userId", validRequest.getUserId().toString())
                    .param("productName", validRequest.getProductName())
                    .param("description", validRequest.getDescription())
                    .param("category", validRequest.getCategory().name())
                    .param("minPrice", validRequest.getMinPrice().toString())
                    .param("status", validRequest.getStatus().name())
                    // .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(result -> {
                        System.out.println(result.getResponse().getContentAsString());
                    })
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.auctionId").value(1))
                    .andExpect(jsonPath("$.status").value("PROCEEDING"))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(status().isCreated());

            verify(auctionService).register(any(RegisterAuctionRequest.class));
        }

        @Test
        @DisplayName("2. 존재하지 않는 사용자로 경매 상품 등록 실패")
        void registerAuction_UserNotFound() throws Exception {
            RegisterAuctionRequest inValidRequest = RegisterAuctionRequest.builder()
                    .userId(100L)
                    .productName("테스트 상품")
                    .description("테스트 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PROCEEDING)
                    .build();

            when(auctionService.register(any(RegisterAuctionRequest.class))).thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

            mockMvc.perform(multipart("/api/v1/auctions/register")
                    .file(image1).file(image2).file(image3)
                    .param("userId", inValidRequest.getUserId().toString())
                    .param("productName", inValidRequest.getProductName())
                    .param("description", inValidRequest.getDescription())
                    .param("category", inValidRequest.getCategory().name())
                    .param("minPrice", inValidRequest.getMinPrice().toString())
                    .param("status", inValidRequest.getStatus().name())
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("상품 사전 등록 테스트")
    class PreRegisterAuctionTest {
        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("1. 유효한 요청으로 상품 사전 등록 성공 응답")
        void preRegisterAuction_Success() throws Exception {
            RegisterAuctionRequest validRequest = RegisterAuctionRequest.builder()
                    .userId(1L)
                    .productName("테스트 상품")
                    .description("테스트 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PENDING)
                    .build();

            RegisterAuctionResponse response = new RegisterAuctionResponse(1L, 1L, PENDING, "success");
            when(auctionService.register(any(RegisterAuctionRequest.class))).thenReturn(response);

            mockMvc.perform(multipart("/api/v1/auctions/register")
                    .file(image1).file(image2).file(image3)
                    .param("userId", validRequest.getUserId().toString())
                    .param("productName", validRequest.getProductName())
                    .param("description", validRequest.getDescription())
                    .param("category", validRequest.getCategory().name())
                    .param("minPrice", validRequest.getMinPrice().toString())
                    .param("status", PENDING.name())
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(result -> {
                        System.out.println(result.getResponse().getContentAsString());
                    })
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.auctionId").value(1))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(status().isCreated());

            verify(auctionService).register(any(RegisterAuctionRequest.class));
        }

        @Test
        @DisplayName("2. 존재하지 않는 사용자로 상품 사전 등록 실패")
        void registerAuction_UserNotFound() throws Exception {
            RegisterAuctionRequest inValidRequest = RegisterAuctionRequest.builder()
                    .userId(999L)
                    .productName("테스트 상품")
                    .description("테스트 설명")
                    .category(ELECTRONICS)
                    .minPrice(10000)
                    .status(PENDING)
                    .build();

            when(auctionService.register(any(RegisterAuctionRequest.class))).thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

            mockMvc.perform(multipart("/api/v1/auctions/register")
                            .file(image1).file(image2).file(image3)
                            .param("userId", inValidRequest.getUserId().toString())
                            .param("productName", inValidRequest.getProductName())
                            .param("description", inValidRequest.getDescription())
                            .param("category", inValidRequest.getCategory().name())
                            .param("minPrice", inValidRequest.getMinPrice().toString())
                            .param("status", inValidRequest.getStatus().name())
                            .with(csrf())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("사전 등록 된 상품 경매 등록 상품으로 전환 테스트")
    class ConvertToAuctionTest {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 등록 된 상품 경매 등록 전환 성공 응답")
        void convertToAuction_Success() throws Exception {
            Long auctionId = 1L;
            Long productId = 1L;
            LocalDateTime endTime = now().plusHours(24);
            StartAuctionResponse response = StartAuctionResponse.of(auctionId, productId, PROCEEDING, endTime);

            when(auctionService.startAuction(eq(auctionId), any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/auctions/{auctionId}/start", auctionId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.auctionId").value(auctionId))
                    .andExpect(jsonPath("$.productId").value(productId))
                    .andExpect(jsonPath("$.status").value("PROCEEDING"))
                    .andExpect(jsonPath("$.endTime").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("경매가 성공적으로 시작되었습니다."));

            verify(auctionService).startAuction(eq(auctionId), any());
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품 ID로 전환 시도 실패")
        void convertToAuction_NotFound() throws Exception {
            Long nonExistAuctionId = 999L;
            Long userId = 1L;

            when(auctionService.startAuction(eq(nonExistAuctionId), any())).thenThrow(new AuctionException(AUCTION_NOT_FOUND));

            mockMvc.perform(post("/api/v1/auctions/{auctionId}/start", nonExistAuctionId)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("경매를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("3. 이미 경매 중인 상품 전환 시도 실패")
        void convertToAuction_AlreadyInAuction() throws Exception {
            Long auctionId = 1L;

            when(auctionService.startAuction(eq(auctionId), any())).thenThrow(new AuctionException(INVALID_AUCTION_STATE));

            mockMvc.perform(post("/api/v1/auctions/{auctionId}/start", auctionId)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("경매 상태가 유효하지 않습니다."));
        }
    }

    @Test
    @DisplayName("4. 전환 후 상태와 시간 정보 확인")
    void convertToAuction_CheckStateAndTime() throws Exception {
        Long auctionId = 3L;
        Long productId = 300L;
        LocalDateTime startTime = now();
        LocalDateTime endTime = now().plusHours(24);

        StartAuctionResponse response = StartAuctionResponse.of(auctionId, productId, PROCEEDING, endTime);
        when(auctionService.startAuction(eq(auctionId), any())).thenReturn(response);

        MvcResult result = mockMvc.perform(post("/api/v1/auctions/{auctionId}/start", auctionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auctionId").value(auctionId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.status").value("PROCEEDING"))
                .andExpect(jsonPath("$.endTime").isNotEmpty())
                .andExpect(jsonPath("$.message").value("경매가 성공적으로 시작되었습니다."))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        StartAuctionResponse returnedResponse = objectMapper.readValue(content, StartAuctionResponse.class);

        assertThat(returnedResponse.endTime()).isAfter(startTime);
        assertThat(ChronoUnit.HOURS.between(startTime, returnedResponse.endTime())).isEqualTo(24);
    }

    @Test
    @DisplayName("5. 취소된 사전 등록 상품 전환 시도 실패")
    void convertToAuction_CancelledPreRegistration() throws Exception {
        Long auctionId = 4L;
        Long userId = 1L;

        when(auctionService.startAuction(eq(auctionId), any())).thenThrow(new AuctionException(INVALID_AUCTION_STATE));

        mockMvc.perform(post("/api/v1/auctions/{auctionId}/start", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(userId.toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("경매 상태가 유효하지 않습니다."));

        verify(auctionService).startAuction(eq(auctionId), any());
    }

}
