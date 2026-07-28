package com.seeat.seeatapi.domain.product.controller;

import com.seeat.seeatapi.domain.product.dto.request.ProductCreateRequest;
import com.seeat.seeatapi.domain.product.dto.request.ProductFaqCreateRequest;
import com.seeat.seeatapi.domain.product.dto.request.ProductUpdateRequest;
import com.seeat.seeatapi.domain.product.dto.response.*;
import com.seeat.seeatapi.domain.product.service.ProductService;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.response.ApiResponse;
import com.seeat.seeatapi.global.response.PageResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final MemberRepository memberRepository;

    public ProductController(ProductService productService, MemberRepository memberRepository) {
        this.productService = productService;
        this.memberRepository = memberRepository;
    }

    // 2-1 수산물 상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
            @CurrentMemberId Long sellerId,
            @Valid @ModelAttribute ProductCreateRequest request,
            @RequestParam List<MultipartFile> images
    ) {
        Member seller = memberRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ProductCreateResponse response = productService.createProduct(seller, request, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "상품이 등록되었습니다."));
    }

    // 2-2 수산물 분류 조회
    @SecurityRequirements
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(productService.getCategoryTree()));
    }

    // 2-3 필터 및 정렬 검색
    @SecurityRequirements
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductSearchResponse>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String storageType,
            @RequestParam(required = false, defaultValue = "LATEST") String sort,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                productService.search(category, origin, priceMin, priceMax, storageType, sort, pageable)
        );
    }

    // 2-4 상품 문의 등록
    @PostMapping("/{productId}/faqs")
    public ResponseEntity<ApiResponse<Object>> createFaq(
            @CurrentMemberId Long memberId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductFaqCreateRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        productService.createFaq(member, productId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "문의가 등록되었습니다."));
    }

    // 2-4 상품 문의 조회
    @GetMapping("/{productId}/faqs")
    public ResponseEntity<PageResponse<ProductFaqResponse>> getFaqs(
            @PathVariable Long productId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getFaqs(productId, pageable));
    }

    // 2-5 상품 정보 수정
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductUpdateResponse>> updateProduct(
            @CurrentMemberId Long sellerId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductUpdateResponse response = productService.updateProduct(sellerId, productId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "상품 정보가 수정되었습니다."));
    }

    // 2-6 상품 이미지 개별 추가
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImages(
            @CurrentMemberId Long sellerId,
            @PathVariable Long productId,
            @RequestParam List<MultipartFile> images
    ) {
        ProductImageResponse response = productService.addImages(sellerId, productId, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "이미지가 추가되었습니다."));
    }

    // 2-7 상품 이미지 개별 삭제
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Object>> deleteImage(
            @CurrentMemberId Long sellerId,
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        productService.deleteImage(sellerId, productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "이미지가 삭제되었습니다."));
    }
}