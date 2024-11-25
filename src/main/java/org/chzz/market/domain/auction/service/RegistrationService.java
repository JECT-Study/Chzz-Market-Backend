package org.chzz.market.domain.auction.service;

import java.util.List;
import org.chzz.market.domain.auction.dto.request.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface RegistrationService {
    void register(Long userId, RegisterRequest request, List<MultipartFile> images);
}
