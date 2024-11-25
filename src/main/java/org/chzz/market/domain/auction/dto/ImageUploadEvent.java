package org.chzz.market.domain.auction.dto;

import java.util.List;
import org.chzz.market.domain.auction.entity.Auction;
import org.springframework.web.multipart.MultipartFile;

public record ImageUploadEvent(Auction auction, List<MultipartFile> images) {
}
