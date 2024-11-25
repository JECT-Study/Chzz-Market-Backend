package org.chzz.market.domain.auctionv2.dto;

import java.util.Map;
import org.chzz.market.domain.auctionv2.dto.request.UpdateAuctionRequest;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.springframework.web.multipart.MultipartFile;

public record AuctionImageUpdateEvent(AuctionV2 auction,
                                      UpdateAuctionRequest request,
                                      Map<String, MultipartFile> imageBuffer) {
}
