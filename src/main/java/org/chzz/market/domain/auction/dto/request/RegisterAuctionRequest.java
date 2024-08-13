package org.chzz.market.domain.auction.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class RegisterAuctionRequest extends BaseRegisterRequest {
    @Override
    public void validate() {
        // TODO: 경매 등록 요청 검증 로직 추가
    }
}
