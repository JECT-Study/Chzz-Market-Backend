package org.chzz.market.domain.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.util.ApplicationContextProvider;
import org.chzz.market.domain.auction.service.AuctionRegistrationService;
import org.chzz.market.domain.auction.service.PreAuctionRegistrationService;
import org.chzz.market.domain.auction.service.RegistrationService;

@Getter
@AllArgsConstructor
public enum AuctionRegisterType {
    PRE_REGISTER(PreAuctionRegistrationService.class),
    REGISTER(AuctionRegistrationService.class);

    private final Class<? extends RegistrationService> serviceClass;

    public RegistrationService getService() {
        return ApplicationContextProvider.getBean(serviceClass);
    }
}
