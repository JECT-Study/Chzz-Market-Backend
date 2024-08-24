package org.chzz.market.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    @Size(max = 15, message = "닉네임은 최대 15자까지 가능합니다.")
    private String nickname;

    @Size(max = 150, message = "자기소개는 최대 500자까지 가능합니다.")
    private String description;

    @Size(max = 50, message = "지역은 최대 10자까지 가능합니다.")
    private String region;

    @Size(max = 100, message = "URL 최대 100자까지 가능합니다.")
    private String url;

}
