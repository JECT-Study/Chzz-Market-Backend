package org.chzz.market.domain.auction.dto;

import java.util.Map;
import org.chzz.market.domain.auction.dto.request.UpdateAuctionRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.springframework.web.multipart.MultipartFile;

public record AuctionImageUpdateEvent(Auction auction,
                                      UpdateAuctionRequest request,
                                      Map<String, MultipartFile> imageBuffer) {
}
