package org.chzz.market.domain.auctionv2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.util.ApplicationContextProvider;
import org.chzz.market.domain.auctionv2.service.AuctionRegistrationService;
import org.chzz.market.domain.auctionv2.service.PreAuctionRegistrationService;
import org.chzz.market.domain.auctionv2.service.RegistrationService;

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
