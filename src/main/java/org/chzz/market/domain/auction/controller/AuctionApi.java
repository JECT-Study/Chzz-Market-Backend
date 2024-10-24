package org.chzz.market.domain.auction.controller;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.Const.AUCTION_ALREADY_REGISTERED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.springdoc.ApiExceptionExplanation;
import org.chzz.market.common.springdoc.ApiResponseExplanations;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserEndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "auctions", description = "경매 API")
public interface AuctionApi {

    @Operation(summary = "경매 목록 조회")
    ResponseEntity<Page<AuctionResponse>> getAuctionList(Category category, Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "베스트 경매 목록 조회")
    ResponseEntity<List<AuctionResponse>> bestAuctionList();

    @Operation(summary = "마감 임박 경매 목록 조회")
    ResponseEntity<List<AuctionResponse>> imminentAuctionList();

    @Operation(summary = "내가 성공한 경매 목록 조회")
    ResponseEntity<Page<WonAuctionResponse>> getWonAuctionHistory(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "내가 실패한 경매 조회")
    ResponseEntity<Page<LostAuctionResponse>> getLostAuctionHistory(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "경매 상세 조회",
            description = "주문 여부는 민감한 정보이므로 낙찰자와 판매자인 경우에만 해당 정보를 확인할 수 있습니다. " +
                    "그 외의 사용자에게는 주문 여부 필드는 응답에 포함되지 않습니다.")
    ResponseEntity<AuctionDetailsResponse> getAuctionDetails(Long auctionId, Long userId);

    @Operation(summary = "경매 입찰 목록 조회")
    ResponseEntity<Page<BidInfoResponse>> getBids(Long userId, Long auctionId, @ParameterObject Pageable pageable);

    @Operation(summary = "낙찰 정보 조회")
    ResponseEntity<WonAuctionDetailsResponse> getWinningBid(Long userId, Long auctionId);

    @Operation(summary = "내가 등록한 모든 경매 목록 조회(현재 사용 X)")
    ResponseEntity<Page<UserAuctionResponse>> getUserRegisteredAuction(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "특정 닉네임 사용자의 모든 경매 상품 목록 조회(현재 사용 X)")
    ResponseEntity<Page<UserAuctionResponse>> getUserAuctionList(String nickname, @ParameterObject Pageable pageable);

    @Operation(summary = "내가 등록한 진행 중인 경매 목록 조회")
    ResponseEntity<Page<UserAuctionResponse>> getProceedingAuctions(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "내가 등록한 종료된 경매 목록 조회")
    ResponseEntity<Page<UserEndedAuctionResponse>> getEndedAuctions(Long userId, Pageable pageable);

    @Operation(summary = "경매 등록")
    ResponseEntity<RegisterResponse> registerAuction(Long userId, @Valid BaseRegisterRequest request,
                                                     List<MultipartFile> images);

    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(
                            value = AuctionErrorCode.class,
                            constant = AUCTION_ALREADY_REGISTERED,
                            name = "이미 등록된 경매일때",
                            description = "이미 등록된 경매 일때 에러가 발생"
                    ),
            }
    )
    @Operation(summary = "정식 경매 전환")
    ResponseEntity<StartAuctionResponse> startAuction(Long userId, @Valid StartAuctionRequest request);

    @Operation(summary = "테스트 경매 등록")
    ResponseEntity<Void> testEndAuction(Long userId, int seconds);
}
