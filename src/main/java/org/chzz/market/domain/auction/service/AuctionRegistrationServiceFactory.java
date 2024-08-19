package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.springframework.stereotype.Component;

import static org.chzz.market.domain.auction.dto.AuctionType.PRE_REGISTER;
import static org.chzz.market.domain.auction.dto.AuctionType.REGISTER;

@Component
@RequiredArgsConstructor
public class AuctionRegistrationServiceFactory {
    private final PreRegisterService preRegisterService;
    private final AuctionRegisterService auctionRegisterService;

    public AuctionRegistrationService getService(BaseRegisterRequest.AuctionType type) {
        switch (type) {
            case PRE_REGISTER:
                return preRegisterService;
            case REGISTER:
                return auctionRegisterService;
            default:
                throw new IllegalArgumentException("알 수 없는 등록 유형 : " + type);
        }
    }
}
