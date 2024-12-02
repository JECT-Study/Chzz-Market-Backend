package org.chzz.market.domain.auction.service;

import org.chzz.market.domain.auction.dto.request.RegisterRequest;

public interface RegistrationService {
    void register(Long userId, RegisterRequest request);
}
