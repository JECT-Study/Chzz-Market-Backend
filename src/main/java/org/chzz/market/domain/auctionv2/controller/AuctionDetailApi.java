package org.chzz.market.domain.auctionv2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.like.dto.LikeResponse;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "auctions(v2)", description = "V2 경매 API")
@RequestMapping("/v2/auctions/{auctionId}")
public interface AuctionDetailApi {
    @Operation(summary = "특정 경매 상세 조회", description = "특정 경매 상세 정보를 조회합니다.") // TODO: 정식경매와 사전 경매 응답 구분 추가
    @GetMapping
    ResponseEntity<?> getAuctionDetails(@LoginUser Long userId,
                                        @PathVariable Long auctionId);

    @Operation(summary = "특정 경매 입찰 목록 조회", description = "특정 경매 입찰 목록을 조회합니다.")
    @GetMapping("/bids")
    ResponseEntity<Page<BidInfoResponse>> getBids(@LoginUser Long userId,
                                                  @PathVariable Long auctionId,
                                                  @ParameterObject @PageableDefault Pageable pageable); // TODO: 내림차순 디폴트

    @Operation(summary = "특정 경매 낙찰 조회", description = "특정 경매 낙찰 정보를 조회합니다.")
    @GetMapping("/won")
    ResponseEntity<WonAuctionDetailsResponse> getWinningBid(@LoginUser Long userId,
                                                            @PathVariable Long auctionId);

    @Operation(summary = "특정 경매 전환", description = "특정 사전 경매를 정식 경매로 전환합니다.")
    @PostMapping("/start")
    ResponseEntity<StartAuctionResponse> startAuction(@LoginUser Long userId,
                                                      @PathVariable Long auctionId);

    @Operation(summary = "특정 경매 좋아요(찜) 요청 및 취소", description = "특정 경매에 대한 좋아요(찜) 요청 및 취소를 합니다.")
    @PostMapping("/likes")
    ResponseEntity<LikeResponse> likeAuction(@LoginUser Long userId,
                                             @PathVariable Long auctionId);

    @Operation(summary = "특정 경매 수정", description = "특정 경매를 수정합니다.")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UpdateProductResponse> updateAuction(@LoginUser Long userId,
                                                        @PathVariable Long auctionId,
                                                        @RequestPart @Valid UpdateProductRequest request,
                                                        @RequestParam(required = false) Map<String, MultipartFile> images);

    @Operation(summary = "특정 경매 삭제", description = "특정 경매를 삭제합니다.")
    @DeleteMapping
    ResponseEntity<DeleteProductResponse> deleteAuction(@LoginUser Long userId,
                                                        @PathVariable Long auctionId);

}
