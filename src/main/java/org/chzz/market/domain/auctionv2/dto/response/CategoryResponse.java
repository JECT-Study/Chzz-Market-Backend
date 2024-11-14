package org.chzz.market.domain.auctionv2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryResponse(
        @Schema(description = "카테고리 값", example = "fashion-and-clothing") String code,
        @Schema(description = "카테고리 이름", example = "패션 및 의류") String displayName) {
}
