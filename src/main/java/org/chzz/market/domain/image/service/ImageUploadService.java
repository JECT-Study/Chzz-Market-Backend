package org.chzz.market.domain.image.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.aws.BucketPrefix;
import org.chzz.market.domain.image.dto.response.CreatePresignedUrlResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private static final int DURATION_MILLIS = 1000 * 60 * 2;

    private final AmazonS3 amazonS3;
    private final String s3BucketName;

    public CreatePresignedUrlResponse createPresignedUrl(BucketPrefix bucketPrefix, String fileName) {
        Date expiration = getPreSignedUrlExpiration();
        String objectKey = bucketPrefix.createPath(fileName);
        GeneratePresignedUrlRequest request = getGeneratePreSignedUrlRequest(objectKey, expiration);
        URL url = amazonS3.generatePresignedUrl(request);
        return CreatePresignedUrlResponse.of(objectKey, url.toString(), expiration);
    }

    public List<CreatePresignedUrlResponse> createAuctionPresignedUrls(final List<String> requests) {
        return requests.stream()
                .map(fileName -> createPresignedUrl(BucketPrefix.AUCTION, fileName))
                .toList();
    }

    private GeneratePresignedUrlRequest getGeneratePreSignedUrlRequest(final String fileName, final Date expiration) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(s3BucketName, fileName)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);
        return generatePresignedUrlRequest;
    }

    private Date getPreSignedUrlExpiration() {
        return Date.from(Instant.now().plusMillis(DURATION_MILLIS));
    }
}
