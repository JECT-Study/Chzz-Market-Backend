package org.chzz.market.domain.imagev2.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chzz.market.domain.image.error.ImageErrorCode.IMAGE_DELETE_FAILED;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import java.util.List;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.service.ImageDeleteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageDeleteServiceTest {
    private static final String ERROR_CODE = "errorCode";
    private final String bucket = "test-bucket";
    private final String cdnPath1 = "https://test-bucket.s3.amazonaws.com/image1.jpg";
    private final String cdnPath2 = "https://test-bucket.s3.amazonaws.com/image2.jpg";

    @Mock
    private AmazonS3 amazonS3Client;

    private ImageDeleteService imageDeleteService;

    @BeforeEach
    void setUp() {
        imageDeleteService = new ImageDeleteService(amazonS3Client, bucket);
    }

    @Test
    void 이미지_삭제_성공() {
        // given
        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);

        when(image1.getCdnPath()).thenReturn(cdnPath1);
        when(image2.getCdnPath()).thenReturn(cdnPath2);

        // when
        imageDeleteService.deleteImages(List.of(image1, image2));

        // then
        verify(amazonS3Client, times(1)).deleteObject(bucket, "image1.jpg");
        verify(amazonS3Client, times(1)).deleteObject(bucket, "image2.jpg");
    }

    @Test
    void S3_에러로_이미지삭제시_예외발생() {
        // given
        Image image = mock(Image.class);
        when(image.getCdnPath()).thenReturn(cdnPath1);

        doThrow(AmazonServiceException.class).when(amazonS3Client).deleteObject(bucket, "image1.jpg");

        // when & then
        assertThatThrownBy(() -> imageDeleteService.deleteImages(List.of(image)))
                .isInstanceOf(ImageException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(IMAGE_DELETE_FAILED);
    }

    @Test
    void 잘못된_URL_이미지_삭제시_예외발생() {
        // given
        Image image = mock(Image.class);
        when(image.getCdnPath()).thenReturn("invalid-url");

        // when & then
        assertThatThrownBy(() -> imageDeleteService.deleteImages(List.of(image)))
                .isInstanceOf(ImageException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(IMAGE_DELETE_FAILED);
    }
}
