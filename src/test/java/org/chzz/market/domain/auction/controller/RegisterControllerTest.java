package org.chzz.market.domain.auction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;
import org.chzz.market.domain.auction.service.RegisterService;
import org.chzz.market.domain.user.entity.User;
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

import static org.chzz.market.domain.auction.entity.Auction.*;
import static org.chzz.market.domain.product.entity.Product.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AWSConfig.class)
public class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterService registerService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile image1, image2, image3;
    private RegisterRequest validRequest;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

        image1 = new MockMultipartFile("images", "image1.jpg", "image/jpg", "image1".getBytes());
        image2 = new MockMultipartFile("images", "image2.jpg", "image/jpg", "image2".getBytes());
        image3 = new MockMultipartFile("images", "image3.jpg", "image/jpg", "image3".getBytes());

        validRequest = RegisterRequest.builder()
                .userId(1L)
                .productName("테스트 상품")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .minPrice(1000)
                .build();
    }

    @Nested
    @DisplayName("상품 경매 등록 테스트")
    class RegisterAuctionTest {

        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("상품 경매 등록 - 모든 필드 정상 입력 시 성공 응답")
        void registerAuction_Success() throws Exception {

            RegisterResponse response = new RegisterResponse(1L, 1L, AuctionStatus.PROCEEDING, "success");
            when(registerService.register(any(RegisterRequest.class), eq(AuctionStatus.PROCEEDING))).thenReturn(response);

            mockMvc.perform(multipart("/api/v1/register/auction")
                    .file(image1).file(image2).file(image3)
                    .param("userId", validRequest.getUserId().toString())
                    .param("productName", validRequest.getProductName())
                    .param("description", validRequest.getDescription())
                    .param("category", validRequest.getCategory().name())
                    .param("minPrice", validRequest.getMinPrice().toString())
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

            verify(registerService).register(any(RegisterRequest.class), eq(AuctionStatus.PROCEEDING));
        }
    }

    @Nested
    @DisplayName("상품 사전 등록 테스트")
    class PreRegisterAuctionTest {
        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("상품 사전 등록 - 모든 필드 정상 입력 시 성공 응답")
        void preRegisterAuction_Success() throws Exception {
            RegisterResponse response = new RegisterResponse(1L, 1L, AuctionStatus.PENDING, "success");
            when(registerService.register(any(RegisterRequest.class), eq(AuctionStatus.PENDING))).thenReturn(response);

            mockMvc.perform(multipart("/api/v1/register/pre-auction")
                    .file(image1).file(image2).file(image3)
                    .param("userId", validRequest.getUserId().toString())
                    .param("productName", validRequest.getProductName())
                    .param("description", validRequest.getDescription())
                    .param("category", validRequest.getCategory().name())
                    .param("minPrice", validRequest.getMinPrice().toString())
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

            verify(registerService).register(any(RegisterRequest.class), eq(AuctionStatus.PENDING));
        }
    }

}
