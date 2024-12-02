package org.chzz.market.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.chzz.market.domain.image.dto.response.CreatePresignedUrlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "image", description = "이미지 API")
@RequestMapping("/v1/image")
public interface ImageApi {

    @Operation(
            summary = "사용자 프로필 이미지 업로드를 위한 인증 url 발급",
            description = """
                    사용자가 프로필 이미지를 업로드 하기 위해 S3에 업로드 권한을 url 형태로 발급 받습니다.
                    url의 인증 만료 시간은 2분입니다.
                    """
    )
    @ApiResponses(value = {

    })
    public ResponseEntity<CreatePresignedUrlResponse> createProfileImagePresignedUrl(@RequestBody String fileName);

    @Operation(
            summary = "경매 이미지들을 업로드하기 위한 인증 url 발급",
            description = """
                    경매 이미지들를 업로드 하기 위해 S3에 업로드 권한을 url 형태로 발급 받습니다.
                    이미지 하나당 인증 url이 하나씩 필요합니다.
                    url의 인증 만료 시간은 2분입니다.
                    """
    )
    @ApiResponses(value = {

    })
    public ResponseEntity<List<CreatePresignedUrlResponse>> createAuctionPresignedUrls(
            @RequestBody List<String> requests);
}
