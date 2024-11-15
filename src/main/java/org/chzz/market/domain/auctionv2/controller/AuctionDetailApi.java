package org.chzz.market.domain.auctionv2.controller;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.Const.AUCTION_ACCESS_FORBIDDEN;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.Const.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.Const.OFFICIAL_AUCTION_DELETE_FORBIDDEN;
import static org.chzz.market.domain.imagev2.error.ImageErrorCode.Const.IMAGE_DELETE_FAILED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.springdoc.ApiExceptionExplanation;
import org.chzz.market.common.springdoc.ApiResponseExplanations;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.error.AuctionErrorCode;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.imagev2.error.ImageErrorCode;
import org.chzz.market.domain.like.dto.LikeResponse;
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
    ResponseEntity<Void> startAuction(@LoginUser Long userId,
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

    @Operation(summary = "특정 경매 삭제", description = "특정 경매를 삭제합니다. 삭제는 사전경매만 가능합니다.")
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = OFFICIAL_AUCTION_DELETE_FORBIDDEN, name = "정식 경매 인 경우"),
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = AUCTION_ACCESS_FORBIDDEN, name = "경매의 접근 권한이 없는 경우"),
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = AUCTION_NOT_FOUND, name = "경매를 찾을 수 없는 경우"),
                    @ApiExceptionExplanation(value = ImageErrorCode.class, constant = IMAGE_DELETE_FAILED, name = "이미지 삭제에 실패한 경우"),
            }
    )
    @DeleteMapping
    ResponseEntity<Void> deleteAuction(@LoginUser Long userId,
                                       @PathVariable Long auctionId);
}
