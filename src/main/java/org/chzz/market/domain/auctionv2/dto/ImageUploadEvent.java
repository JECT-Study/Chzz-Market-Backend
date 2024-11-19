package org.chzz.market.domain.auctionv2.dto;

import java.util.List;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.springframework.web.multipart.MultipartFile;

public record ImageUploadEvent(AuctionV2 auction, List<MultipartFile> images) {
}
