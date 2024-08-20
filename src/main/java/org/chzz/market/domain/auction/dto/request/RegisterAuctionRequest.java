package org.chzz.market.domain.auction.dto.request;

import lombok.Setter;

@Setter
public class RegisterAuctionRequest extends BaseRegisterRequest {
    @Override
    public void validate() {
        // TODO: 경매 등록 요청 검증 로직 추가
    }
}
