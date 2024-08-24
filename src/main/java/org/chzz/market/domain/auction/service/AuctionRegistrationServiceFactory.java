package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.enums.AuctionRegisterType;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.service.register.AuctionRegisterService;
import org.chzz.market.domain.auction.service.register.AuctionRegistrationService;
import org.chzz.market.domain.auction.service.register.PreRegisterService;
import org.springframework.stereotype.Component;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.UNKNOWN_AUCTION_TYPE;

@Component
@RequiredArgsConstructor
public class AuctionRegistrationServiceFactory {
    private final PreRegisterService preRegisterService;
    private final AuctionRegisterService auctionRegisterService;

    public AuctionRegistrationService getService(AuctionRegisterType type) {
        return switch (type) {
            case PRE_REGISTER -> preRegisterService;
            case REGISTER -> auctionRegisterService;
            default -> throw new AuctionException(UNKNOWN_AUCTION_TYPE);
        };
    }
}
