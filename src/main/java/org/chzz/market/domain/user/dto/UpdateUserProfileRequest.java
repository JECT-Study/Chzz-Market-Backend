package org.chzz.market.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 15, message = "닉네임은 최대 15자까지 가능합니다.")
    private String nickname;

    private String bio;

    @Pattern(regexp = "^(http|https)://.*$|^$", message = "링크는 유효한 URL 형식이어야 합니다.")
    private String link;
}
