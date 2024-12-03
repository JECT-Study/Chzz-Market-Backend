package org.chzz.market.domain.image.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.aws.BucketPrefix;
import org.chzz.market.domain.image.dto.response.CreatePresignedUrlResponse;
import org.chzz.market.domain.image.service.ImageUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/image")
public class ImageController implements ImageApi {
    private final ImageUploadService imageUploadService;

    @Override
    @PostMapping("/profile")
    public ResponseEntity<CreatePresignedUrlResponse> createProfileImagePresignedUrl(final String fileName) {
        CreatePresignedUrlResponse response = imageUploadService.createPresignedUrl(BucketPrefix.PROFILE, fileName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/auction")
    public ResponseEntity<List<CreatePresignedUrlResponse>> createAuctionPresignedUrls(final List<String> requests) {
        List<CreatePresignedUrlResponse> presignedUrls = imageUploadService.createAuctionPresignedUrls(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(presignedUrls);
    }
}
