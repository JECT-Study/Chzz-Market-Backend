package org.chzz.market.domain.auctionv2.controller;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.Const.END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY;
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
import jakarta.validation.constraints.Size;
import java.util.List;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.springdoc.ApiExceptionExplanation;
import org.chzz.market.common.springdoc.ApiResponseExplanations;
import org.chzz.market.common.validation.annotation.NotEmptyMultipartList;
import org.chzz.market.domain.auctionv2.dto.request.RegisterRequest;
import org.chzz.market.domain.auctionv2.dto.response.CategoryResponse;
import org.chzz.market.domain.auctionv2.dto.response.EndedAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.OfficialAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.ProceedingAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.auctionv2.error.AuctionErrorCode;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "auctions(v2)", description = "V2 경매 API")
@RequestMapping("/v2/auctions")
public interface AuctionV2Api {
    @Operation(summary = "경매 목록 조회", description = "경매 목록을 조회합니다. status 파라미터를 통해 조회 유형을 지정합니다.")
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
                                           @ParameterObject @PageableDefault(sort = "newest-v2") Pageable pageable);

    @Operation(summary = "경매 카테고리 조회", description = "경매 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    ResponseEntity<List<CategoryResponse>> getCategoryList();

    @Operation(summary = "사용자가 등록한 진행중인 경매 목록 조회", description = "사용자가 등록한 진행중인 경매 목록을 조회합니다.")
    @GetMapping("/users/proceeding")
    ResponseEntity<Page<ProceedingAuctionResponse>> getUserProceedingAuctionList(@LoginUser Long userId,
                                                                                 @ParameterObject @PageableDefault(sort = "newest-v2") Pageable pageable);

    @Operation(summary = "사용자가 등록한 종료된 경매 목록 조회", description = "사용자가 등록한 종료된 경매 목록을 조회합니다.")
    @GetMapping("/users/ended")
    ResponseEntity<Page<EndedAuctionResponse>> getUserEndedAuctionList(@LoginUser Long userId,
                                                                       @ParameterObject @PageableDefault(sort = "newest-v2") Pageable pageable);

    @Operation(summary = "사용자가 등록한 사전 경매 목록 조회", description = "사용자가 등록한 사전 경매 목록을 조회합니다.")
    @GetMapping("/users/pre")
    ResponseEntity<Page<PreAuctionResponse>> getUserPreAuctionList(@LoginUser Long userId,
                                                                   @ParameterObject @PageableDefault(sort = "newest-v2") Pageable pageable);

    @Operation(summary = "사용자가 낙찰한 경매 목록 조회", description = "사용자가 낙찰한 경매 목록을 조회합니다.")
    @GetMapping("/users/won")
    ResponseEntity<Page<WonAuctionResponse>> getUserWonAuctionList(@LoginUser Long userId,
                                                                   @ParameterObject @PageableDefault(sort = "newest-v2") Pageable pageable);

    @Operation(summary = "사용자가 낙찰실패한 경매 목록 조회", description = "사용자가 낙찰실패한 경매 목록을 조회합니다.")
    @GetMapping("/users/lost")
    ResponseEntity<Page<LostAuctionResponse>> getUserLostAuctionList(@LoginUser Long userId,
                                                                     @ParameterObject @PageableDefault(sort = "newest-v2") Pageable pageable);

    @Operation(summary = "사용자가 좋아요(찜)한 경매 목록 조회", description = "사용자가 좋아요(찜)한 경매 목록을 조회합니다.")
    @GetMapping("/users/likes")
    ResponseEntity<Page<PreAuctionResponse>> getLikedAuctionList(@LoginUser Long userId,
                                                                 @ParameterObject @PageableDefault(sort = "newest-v2") Pageable pageable);

    @Operation(summary = "경매 등록", description = "경매를 등록합니다.")
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = UserErrorCode.class, constant = USER_NOT_FOUND, name = "회원정보 조회 실패"),
            }
    )
    @PostMapping
    ResponseEntity<Void> registerAuction(@LoginUser Long userId,
                                         @RequestPart("request") @Valid RegisterRequest request,
                                         @RequestPart(value = "images") @Valid
                                         @NotEmptyMultipartList @Size(max = 5, message = "이미지는 5장 이내로만 업로드 가능합니다.") List<MultipartFile> images);

    @Operation(summary = "경매 테스트 등록", description = "테스트 등록합니다.")
    @PostMapping("/test")
    ResponseEntity<Void> testEndAuction(@LoginUser Long userId,
                                        @RequestParam int seconds);
}
