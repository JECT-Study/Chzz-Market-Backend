package org.chzz.market.domain.auction.service;


import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;

import static org.chzz.market.domain.auction.entity.Auction.*;

public interface RegisterService {
    RegisterResponse register(RegisterRequest request, AuctionStatus InitialStatus);
}
