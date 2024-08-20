package org.chzz.market.domain.auction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.PreRegisterRequest;
import org.chzz.market.domain.auction.dto.request.RegisterAuctionRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.PreRegisterResponse;
import org.chzz.market.domain.auction.dto.response.RegisterAuctionResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.service.AuctionRegistrationServiceFactory;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.auction.service.register.AuctionRegisterService;
import org.chzz.market.domain.auction.service.register.PreRegisterService;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.auction.entity.Auction.AuctionStatus.*;
import static org.chzz.market.domain.auction.enums.AuctionRegisterType.*;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.*;
import static org.chzz.market.domain.product.entity.Product.Category.*;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    private AuctionRegistrationServiceFactory registrationServiceFactory;

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

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("상품 등록 테스트")
    class RegisterAuctionTest {

        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("1. 유효한 요청으로 경매 상품 등록 성공 응답")
        void registerAuction_Success() throws Exception {
            BaseRegisterRequest validRequest = new RegisterAuctionRequest();
            ReflectionTestUtils.setField(validRequest, "userId", 1L);
            ReflectionTestUtils.setField(validRequest, "productName", "테스트 상품");
            ReflectionTestUtils.setField(validRequest, "description", "테스트 설명");
            ReflectionTestUtils.setField(validRequest, "category", ELECTRONICS);
            ReflectionTestUtils.setField(validRequest, "minPrice", 10000);
            ReflectionTestUtils.setField(validRequest, "auctionRegisterType", REGISTER);

            String requestJson = objectMapper.writeValueAsString(validRequest);

            RegisterAuctionResponse response = RegisterAuctionResponse.of(1L, 1L, PROCEEDING);

            AuctionRegisterService mockService = mock(AuctionRegisterService.class);
            when(mockService.register(any(BaseRegisterRequest.class), anyList())).thenReturn(response);
            when(registrationServiceFactory.getService(REGISTER)).thenReturn(mockService);

            MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());

            mockMvc.perform(multipart("/api/v1/auctions")
                    .file(requestPart)
                    .file(image1).file(image2).file(image3)
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.auctionId").value(1))
                    .andExpect(jsonPath("$.status").value("PROCEEDING"))
                    .andExpect(jsonPath("$.message").value("상품이 성공적으로 경매 등록되었습니다."));

            verify(mockService).register(any(BaseRegisterRequest.class), anyList());
        }

        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("2. 유효한 요청으로 상품 사전 등록 성공 응답")
        void preRegisterAuction_Success() throws Exception {
            BaseRegisterRequest validRequest = new PreRegisterRequest();
            ReflectionTestUtils.setField(validRequest, "userId", 1L);
            ReflectionTestUtils.setField(validRequest, "productName", "테스트 상품");
            ReflectionTestUtils.setField(validRequest, "description", "테스트 설명");
            ReflectionTestUtils.setField(validRequest, "category", ELECTRONICS);
            ReflectionTestUtils.setField(validRequest, "minPrice", 10000);
            ReflectionTestUtils.setField(validRequest, "auctionRegisterType", PRE_REGISTER);

            String requestJson = objectMapper.writeValueAsString(validRequest);

            PreRegisterResponse response = PreRegisterResponse.of(1L);

            PreRegisterService mockService = mock(PreRegisterService.class);
            when(mockService.register(any(BaseRegisterRequest.class), anyList())).thenReturn(response);
            when(registrationServiceFactory.getService(PRE_REGISTER)).thenReturn(mockService);

            MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());

            mockMvc.perform(multipart("/api/v1/auctions")
                    .file(requestPart)
                    .file(image1).file(image2).file(image3)
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.auctionId").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.message").value("상품이 성공적으로 사전 등록되었습니다."));

            verify(mockService).register(any(BaseRegisterRequest.class), anyList());
        }

        @Test
        @DisplayName("3. 존재하지 않는 사용자로 경매 상품 등록 실패")
        void registerAuction_UserNotFound() throws Exception {
            BaseRegisterRequest invalidRequest = new RegisterAuctionRequest();
            ReflectionTestUtils.setField(invalidRequest, "userId", 999L);
            ReflectionTestUtils.setField(invalidRequest, "productName", "테스트 상품");
            ReflectionTestUtils.setField(invalidRequest, "description", "테스트 설명");
            ReflectionTestUtils.setField(invalidRequest, "category", ELECTRONICS);
            ReflectionTestUtils.setField(invalidRequest, "minPrice", 10000);
            ReflectionTestUtils.setField(invalidRequest, "auctionRegisterType", REGISTER);

            String requestJson = objectMapper.writeValueAsString(invalidRequest);

            AuctionRegisterService mockService = mock(AuctionRegisterService.class);
            when(mockService.register(any(BaseRegisterRequest.class), anyList()))
                    .thenThrow(new UserException(USER_NOT_FOUND));
            when(registrationServiceFactory.getService(REGISTER)).thenReturn(mockService);

            MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());

            mockMvc.perform(multipart("/api/v1/auctions")
                    .file(requestPart)
                    .file(image1).file(image2).file(image3)
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("사전 등록 된 상품 경매 등록 상품으로 전환 테스트")
    class ConvertToAuctionTest {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 등록 된 상품 경매 등록 전환 성공 응답")
        void convertToAuction_Success() throws Exception {
            StartAuctionRequest request = new StartAuctionRequest();
            ReflectionTestUtils.setField(request, "productId", 1L);
            ReflectionTestUtils.setField(request, "userId", 1L);

            String requestJson = objectMapper.writeValueAsString(request);

            StartAuctionResponse response = StartAuctionResponse.of(1L, 1L, PROCEEDING, LocalDateTime.now().plusDays(1));
            when(auctionService.startAuction(any(StartAuctionRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auctions/start")
                    .content(requestJson)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.auctionId").value(1))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.status").value("PROCEEDING"))
                    .andExpect(jsonPath("$.endTime").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("경매가 성공적으로 시작되었습니다."));

            verify(auctionService).startAuction(any(StartAuctionRequest.class));
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품 ID로 전환 시도 실패")
        void convertToAuction_NotFound() throws Exception {
            StartAuctionRequest request = new StartAuctionRequest();
            ReflectionTestUtils.setField(request, "productId", 999L);
            ReflectionTestUtils.setField(request, "userId", 1L);

            String requestJson = objectMapper.writeValueAsString(request);

            when(auctionService.startAuction(any(StartAuctionRequest.class)))
                    .thenThrow(new AuctionException(AUCTION_NOT_FOUND));

            mockMvc.perform(post("/api/v1/auctions/start")
                    .content(requestJson)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("경매를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("3. 이미 경매 중인 상품 전환 시도 실패")
        void convertToAuction_AlreadyInAuction() throws Exception {
            StartAuctionRequest request = new StartAuctionRequest();
            ReflectionTestUtils.setField(request, "productId", 1L);
            ReflectionTestUtils.setField(request, "userId", 1L);

            String requestJson = objectMapper.writeValueAsString(request);

            when(auctionService.startAuction(any(StartAuctionRequest.class)))
                    .thenThrow(new AuctionException(AUCTION_ALREADY_REGISTERED));

            mockMvc.perform(post("/api/v1/auctions/start")
                    .content(requestJson)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이미 등록된 경매입니다."));
        }
    }

    @Test
    @DisplayName("4. 전환 후 상태와 시간 정보 확인")
    void convertToAuction_CheckStateAndTime() throws Exception {
        Long productId = 1L;
        LocalDateTime startTime = now();
        LocalDateTime endTime = startTime.plusHours(24);

        StartAuctionRequest request = new StartAuctionRequest();
        ReflectionTestUtils.setField(request, "productId", productId);
        ReflectionTestUtils.setField(request, "userId", 1L);

        StartAuctionResponse response = StartAuctionResponse.of(1L, productId, PROCEEDING, endTime);
        when(auctionService.startAuction(any(StartAuctionRequest.class))).thenReturn(response);

        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/v1/auctions/start")
                .content(requestJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auctionId").value(1))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.status").value("PROCEEDING"))
                .andExpect(jsonPath("$.endTime").isNotEmpty())
                .andExpect(jsonPath("$.message").value("경매가 성공적으로 시작되었습니다."))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        StartAuctionResponse returnedResponse = objectMapper.readValue(content, StartAuctionResponse.class);

        assertThat(returnedResponse.endTime()).isAfter(startTime);
        assertThat(returnedResponse.endTime()).isBefore(startTime.plusHours(25));
        assertThat(ChronoUnit.HOURS.between(startTime, returnedResponse.endTime())).isEqualTo(24);
    }
}
