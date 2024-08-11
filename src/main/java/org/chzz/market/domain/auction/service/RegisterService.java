package org.chzz.market.domain.auction.service;


import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;

public interface RegisterService {
    RegisterResponse register(RegisterRequest request);
}
