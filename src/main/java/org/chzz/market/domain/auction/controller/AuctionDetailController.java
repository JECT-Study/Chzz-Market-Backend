package org.chzz.market.domain.auction.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auction.dto.request.UpdateAuctionRequest;
import org.chzz.market.domain.auction.dto.response.UpdateAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.service.AuctionDeleteService;
import org.chzz.market.domain.auction.service.AuctionDetailService;
import org.chzz.market.domain.auction.service.AuctionModifyService;
import org.chzz.market.domain.auction.service.AuctionStartService;
import org.chzz.market.domain.auction.service.AuctionWonService;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.service.BidLookupService;
import org.chzz.market.domain.like.service.LikeUpdateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auctions/{auctionId}")
public class AuctionDetailController implements AuctionDetailApi {
    private final AuctionDetailService auctionDetailService;
    private final AuctionDeleteService auctionDeleteService;
    private final AuctionModifyService auctionModifyService;
    private final AuctionStartService auctionStartService;
    private final AuctionWonService auctionWonService;
    private final BidLookupService bidLookupService;
    private final LikeUpdateService likeUpdateService;

    @Override
    @GetMapping
    public ResponseEntity<?> getAuctionDetails(@LoginUser Long userId,
                                               @PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionDetailService.getAuctionDetails(userId, auctionId));
    }

    @Override
    @GetMapping("/bids")
    public ResponseEntity<Page<BidInfoResponse>> getBids(@LoginUser Long userId,
                                                         @PathVariable Long auctionId,
                                                         @PageableDefault(sort = "bid-amount", direction = DESC) Pageable pageable) {
        return ResponseEntity.ok(bidLookupService.getBidsByAuctionId(userId, auctionId, pageable));
    }

    @Override
    @GetMapping("/won")
    public ResponseEntity<WonAuctionDetailsResponse> getWinningBid(@LoginUser Long userId,
                                                                   @PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionWonService.getWinningBidByAuctionId(userId, auctionId));
    }

    @Override
    @PostMapping("/start")
    public ResponseEntity<Void> startAuction(@LoginUser Long userId,
                                             @PathVariable Long auctionId) {
        auctionStartService.start(userId, auctionId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/likes")
    public ResponseEntity<Void> likeAuction(@LoginUser Long userId, @PathVariable Long auctionId) {
        likeUpdateService.updateLike(userId, auctionId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateAuctionResponse> updateAuction(Long userId, @PathVariable Long auctionId,
                                                               UpdateAuctionRequest request,
                                                               Map<String, MultipartFile> images) {
        UpdateAuctionResponse response =
                auctionModifyService.updateAuction(userId, auctionId, request, images);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteAuction(Long userId, Long auctionId) {
        auctionDeleteService.delete(userId, auctionId);
        return ResponseEntity.ok().build();
    }
}
