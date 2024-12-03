package org.chzz.market.domain.auction.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.chzz.market.domain.auction.dto.AuctionRegisterType;
import org.chzz.market.domain.auction.dto.request.RegisterRequest;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.image.service.ObjectKeyValidator;
import org.chzz.market.util.AuthenticatedRequestTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;


class AuctionControllerTest extends AuthenticatedRequestTest {
    @MockBean
    ObjectKeyValidator objectKeyValidator;

    @MockBean
    ImageService imageService;

    @Test
    @DisplayName("사전 경매 등록")
    void testPreAuctionRegistration() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "name",
                "description",
                Category.BOOKS_AND_MEDIA,
                10000,
                AuctionRegisterType.PRE_REGISTER,
                List.of("A","B","C"));
        String req = objectMapper.writeValueAsString(request);
        doNothing().when(objectKeyValidator).validate(anyString());

        // when
        mockMvc.perform(post("/api/v1/auctions")
                        .content(req)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isCreated())
                .andDo(print());

        verify(imageService).uploadImages(any());
    }

    @Test
    @DisplayName("이미지가 없는 경우")
    void testRegisterAuctionWithNoImage() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "name",
                "description",
                Category.BOOKS_AND_MEDIA,
                10000,
                AuctionRegisterType.PRE_REGISTER,
                null);
        String req = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/auctions")
                        .content(req)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message[0]").value(containsString("파일은 최소 하나 이상 필요합니다.")))
                .andDo(print());

    }

    @Test
    @DisplayName("이미지가 5개 이상인 경우")
    void testRegisterAuctionWithOverImageCount() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "name",
                "description",
                Category.BOOKS_AND_MEDIA,
                10000,
                AuctionRegisterType.PRE_REGISTER,
                List.of("A","B","C","D","E","F"));
        String req = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/auctions")
                        .content(req)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message[0]").value(containsString("objectKeys: 이미지는 5장 이내로만 업로드 가능합니다.")))
                .andDo(print());
    }
}
