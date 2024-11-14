package org.chzz.market.domain.auctionv2.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.service.AuctionDeleteService;
import org.chzz.market.domain.auctionv2.service.AuctionStartService;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.like.dto.LikeResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AuctionDetailController implements AuctionDetailApi {
    private final AuctionDeleteService auctionDeleteService;
    private final AuctionStartService auctionStartService;

    @Override
    public ResponseEntity<?> getAuctionDetails(Long userId, Long auctionId) {
        return null;
    }

    @Override
    public ResponseEntity<Page<BidInfoResponse>> getBids(Long userId, Long auctionId, Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<WonAuctionDetailsResponse> getWinningBid(Long userId, Long auctionId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> startAuction(Long userId, Long auctionId) {
        auctionStartService.start(userId, auctionId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<LikeResponse> likeAuction(Long userId, Long auctionId) {
        return null;
    }

    @Override
    public ResponseEntity<UpdateProductResponse> updateAuction(Long userId, Long auctionId,
                                                               UpdateProductRequest request,
                                                               Map<String, MultipartFile> images) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteAuction(Long userId, Long auctionId) {
        auctionDeleteService.delete(userId, auctionId);
        return ResponseEntity.ok().build();
    }
}
