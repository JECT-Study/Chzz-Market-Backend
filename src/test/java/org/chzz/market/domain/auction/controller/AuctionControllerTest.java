package org.chzz.market.domain.auction.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.chzz.market.domain.auction.dto.AuctionRegisterType;
import org.chzz.market.domain.auction.dto.request.RegisterRequest;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.util.AuthenticatedRequestTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;


class AuctionControllerTest extends AuthenticatedRequestTest {
    @MockBean
    ImageService imageService;

    RegisterRequest request;

    MockMultipartFile image1, image2, image3, image4, image5, image6;
    MockMultipartFile requestPart;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        request = new RegisterRequest("name", "description", Category.BOOKS_AND_MEDIA, 10000,
                AuctionRegisterType.PRE_REGISTER);
        requestPart = new MockMultipartFile(
                "request", "request", "application/json", objectMapper.writeValueAsBytes(request)
        );

        image1 = new MockMultipartFile("images", "imagefile1.jpeg", "image/jpeg",
                "<<jpeg data>>".getBytes());
        image2 = new MockMultipartFile("images", "imagefile2.jpeg", "image/jpeg",
                "<<jpeg data>>".getBytes());

        image3 = new MockMultipartFile("images", "imagefile3.jpeg", "image/jpeg",
                "<<jpeg data>>".getBytes());
        image4 = new MockMultipartFile("images", "imagefile4.jpeg", "image/jpeg",
                "<<jpeg data>>".getBytes());

        image5 = new MockMultipartFile("images", "imagefile5.jpeg", "image/jpeg",
                "<<jpeg data>>".getBytes());
        image6 = new MockMultipartFile("images", "imagefile6.jpeg", "image/gif",
                "<<gif data>>".getBytes());
    }

    @Test
    @DisplayName("사전 경매 등록")
    void testPreAuctionRegistration() throws Exception {
        // when
        mockMvc.perform(multipart("/api/v1/auctions")
                        .file(requestPart)
                        .file(image1)
                        .file(image2)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isCreated())
                .andDo(print());

        verify(imageService).uploadImages(any());
    }

    @Test
    @DisplayName("이미지가 없는 경우")
    void testRegisterAuctionWithNoImage() throws Exception {

        MockMultipartFile emptyImage = new MockMultipartFile(
                "images", "file", MediaType.MULTIPART_FORM_DATA_VALUE, new byte[0]
        );
        // when
        mockMvc.perform(multipart("/api/v1/auctions")
                        .file(emptyImage)
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message[0]").value(containsString("images: 파일은 최소 하나 이상 필요합니다.")))
                .andDo(print());

    }

    @Test
    @DisplayName("이미지가 5개 이상인 경우")
    void testRegisterAuctionWithOverImageCount() throws Exception {
        // given
        mockMvc.perform(multipart("/api/v1/auctions")
                        .file(requestPart)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .file(image4)
                        .file(image5)
                        .file(image6)
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message[0]").value(containsString("images: 이미지는 5장 이내로만 업로드 가능합니다.")))
                .andDo(print());
    }
}
