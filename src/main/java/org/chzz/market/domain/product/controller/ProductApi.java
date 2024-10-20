package org.chzz.market.domain.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.chzz.market.domain.like.dto.LikeResponse;
import org.chzz.market.domain.product.dto.CategoryResponse;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "products", description = "사전 경매 API")
public interface ProductApi {

    @Operation(summary = "사전 경매 목록 조회")
    ResponseEntity<Page<ProductResponse>> getProductList(Category category, Long userId,
                                                         @ParameterObject Pageable pageable);

    @Operation(summary = "상품 카테고리 목록 조회")
    ResponseEntity<List<CategoryResponse>> getCategoryList();

    @Operation(summary = "사전 경매 상세 조회")
    ResponseEntity<ProductDetailsResponse> getProductDetails(Long productId, Long userId);

    @Operation(summary = "특정 닉네임 사용자의 사전 경매 목록 조회 (현재 사용 x)")
    ResponseEntity<Page<ProductResponse>> getMyProductList(String nickname, @ParameterObject Pageable pageable);

    @Operation(summary = "나의 사전 경매 목록 조회")
    ResponseEntity<Page<ProductResponse>> getRegisteredProductList(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "나의 좋아요 사전 경매 목록 조회")
    ResponseEntity<Page<ProductResponse>> getLikedProductList(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "사전 경매 수정")
    @Parameter(
            name = "sequence (예: 1)",
            description = "key: 이미지 순서(1~5), value: 업로드할 이미지 파일",
            schema = @Schema(type = "string", format = "binary")
    )
    ResponseEntity<UpdateProductResponse> updateProduct(Long userId, Long productId, UpdateProductRequest request,
                                                        Map<String, MultipartFile> images);

    @Operation(summary = "사전 경매 삭제")
    ResponseEntity<DeleteProductResponse> deleteProduct(Long productId, Long userId);

    @Operation(summary = "좋아요 요청 및 취소")
    ResponseEntity<LikeResponse> toggleProductLike(Long productId, Long userId);
}
