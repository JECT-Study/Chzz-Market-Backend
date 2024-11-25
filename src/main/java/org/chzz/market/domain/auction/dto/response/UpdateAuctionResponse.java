package org.chzz.market.domain.auction.dto.response;

import java.util.List;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.image.dto.response.ImageResponse;

public record UpdateAuctionResponse(
        Long auctionId,
        String auctionName,
        String description,
        Category category,
        Integer minPrice,
        List<ImageResponse> imageUrls
) {
    public static UpdateAuctionResponse from(Auction auction) {
        return new UpdateAuctionResponse(
                auction.getId(),
                auction.getName(),
                auction.getDescription(),
                auction.getCategory(),
                auction.getMinPrice(),
                auction.getImages()
                        .stream()
                        .map(ImageResponse::from)
                        .toList()
        );
    }
}
