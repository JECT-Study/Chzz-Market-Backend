package org.chzz.market.domain.auction.controller;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.Const.END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY;
import static org.chzz.market.domain.user.error.UserErrorCode.Const.USER_NOT_FOUND;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.springdoc.ApiExceptionExplanation;
import org.chzz.market.common.springdoc.ApiResponseExplanations;
import org.chzz.market.domain.auction.dto.request.RegisterRequest;
import org.chzz.market.domain.auction.dto.response.CategoryResponse;
import org.chzz.market.domain.auction.dto.response.EndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.OfficialAuctionResponse;
import org.chzz.market.domain.auction.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auction.dto.response.ProceedingAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "auctions", description = "경매 API")
@RequestMapping("/v1/auctions")
public interface AuctionApi {
    @Operation(
            summary = "경매 목록 조회",
            description = "경매 목록을 조회합니다. status 파라미터를 통해 조회 유형을 지정합니다. 정렬 기준 (sort 파라미터): " +
                    "popularity(인기순), likes(좋아요순), expensive(높은 가격순), cheap(낮은 가격순), " +
                    "immediately(즉시 종료순), newest(최신순)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정식 경매 응답(페이징)",
                    content = {@Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OfficialAuctionResponse.class))
                    )}
            ),
            @ApiResponse(responseCode = "201", description = "사전 경매 응답(페이징)",
                    content = {@Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PreAuctionResponse.class))
                    )}
            )
    })
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY, name = "minutes 파라미터는 진행중인 경매일 때만 사용가능"),
            }
    )
    @GetMapping
    ResponseEntity<Page<?>> getAuctionList(@LoginUser Long userId, @RequestParam(required = false) Category category,
                                           @RequestParam(required = false, defaultValue = "proceeding") AuctionStatus status,
                                           @RequestParam(required = false) @Min(value = 1, message = "minutes는 1 이상의 값이어야 합니다.") Integer minutes,
                                           @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "경매 카테고리 조회", description = "경매 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    ResponseEntity<List<CategoryResponse>> getCategoryList();

    @Operation(summary = "사용자가 등록한 진행중인 경매 목록 조회", description = "사용자가 등록한 진행중인 경매 목록을 조회합니다.")
    @GetMapping("/users/proceeding")
    ResponseEntity<Page<ProceedingAuctionResponse>> getUserProceedingAuctionList(@LoginUser Long userId,
                                                                                 @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "사용자가 등록한 종료된 경매 목록 조회", description = "사용자가 등록한 종료된 경매 목록을 조회합니다.")
    @GetMapping("/users/ended")
    ResponseEntity<Page<EndedAuctionResponse>> getUserEndedAuctionList(@LoginUser Long userId,
                                                                       @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "사용자가 등록한 사전 경매 목록 조회", description = "사용자가 등록한 사전 경매 목록을 조회합니다.")
    @GetMapping("/users/pre")
    ResponseEntity<Page<PreAuctionResponse>> getUserPreAuctionList(@LoginUser Long userId,
                                                                   @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "사용자가 낙찰한 경매 목록 조회", description = "사용자가 낙찰한 경매 목록을 조회합니다.")
    @GetMapping("/users/won")
    ResponseEntity<Page<WonAuctionResponse>> getUserWonAuctionList(@LoginUser Long userId,
                                                                   @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "사용자가 낙찰실패한 경매 목록 조회", description = "사용자가 낙찰실패한 경매 목록을 조회합니다.")
    @GetMapping("/users/lost")
    ResponseEntity<Page<LostAuctionResponse>> getUserLostAuctionList(@LoginUser Long userId,
                                                                     @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "사용자가 좋아요(찜)한 경매 목록 조회", description = "사용자가 좋아요(찜)한 경매 목록을 조회합니다.")
    @GetMapping("/users/likes")
    ResponseEntity<Page<PreAuctionResponse>> getLikedAuctionList(@LoginUser Long userId,
                                                                 @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "경매 등록", description = "경매를 등록합니다.")
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = UserErrorCode.class, constant = USER_NOT_FOUND, name = "회원정보 조회 실패"),
            }
    )
    @PostMapping
    ResponseEntity<Void> registerAuction(@LoginUser Long userId,
                                         @RequestBody @Valid RegisterRequest request);

    @Operation(summary = "경매 테스트 등록", description = "테스트 등록합니다.")
    @PostMapping("/test")
    ResponseEntity<Void> testEndAuction(@LoginUser Long userId,
                                        @RequestParam int seconds);
}
