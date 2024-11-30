package org.chzz.market.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


@TestConfiguration
public class AWSConfig {

    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        AmazonS3 s3 = Mockito.mock(AmazonS3.class);
        ListObjectsV2Result result = new ListObjectsV2Result();
        result.setPrefix("auction");
        result.setPrefix("profile");
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(result);
        return s3;
    }
}
