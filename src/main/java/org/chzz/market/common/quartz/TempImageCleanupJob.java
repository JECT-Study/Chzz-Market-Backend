package org.chzz.market.common.quartz;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempImageCleanupJob {

    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final static int maxTempImageCount = 50;

    @Scheduled(cron = "0 8 4 * * ?", zone = "Asia/Seoul")
    public void cleanupTempImages() {
        try {
            ListObjectsV2Result result = amazonS3Client.listObjectsV2(bucket, "temp/");
            List<S3ObjectSummary> objects = result.getObjectSummaries();

            if (objects.size() > maxTempImageCount) {
                // 삭제할 객체 키 목록 생성
                List<KeyVersion> keys = objects.stream()
                        .map(object -> new KeyVersion(object.getKey()))
                        .toList();

                // 벌크 삭제 요청 생성
                DeleteObjectsRequest deleteAllRequest = new DeleteObjectsRequest(bucket)
                        .withKeys(keys)
                        .withQuiet(false);

                // 벌크 삭제 실행
                DeleteObjectsResult deleteObjectsResult = amazonS3Client.deleteObjects(deleteAllRequest);

                log.info("임시 이미지 파일 {}개 삭제 완료", deleteObjectsResult.getDeletedObjects().size());
            }
        } catch (Exception e) {
            log.error("임시 이미지 파일 삭제 중 오류 발생", e);
        }
    }
}
