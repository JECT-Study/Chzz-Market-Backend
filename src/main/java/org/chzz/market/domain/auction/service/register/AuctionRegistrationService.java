package org.chzz.market.domain.auction.service.register;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuctionRegistrationService {
    RegisterResponse register(BaseRegisterRequest request, List<MultipartFile> images);
}
