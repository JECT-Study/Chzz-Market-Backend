package org.chzz.market.common.config.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 이미지가 업로드 가능한 파일 목록 세팅
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3PrefixVerifier {
    private static final String DELIMITER = "/";

    private final AmazonS3 s3;
    private final String bucket;

    @EventListener(ApplicationReadyEvent.class)
    private boolean verifyPrefix() {
        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withDelimiter(DELIMITER);
        ListObjectsV2Result result = s3.listObjectsV2(req);
        return result.getCommonPrefixes().stream()
                .map(prefix -> prefix.split(DELIMITER)[0])
                .peek(prefix -> log.info("bucket prefix: {}", prefix))
                .allMatch(BucketPrefix::hasNameOf);
    }
}
