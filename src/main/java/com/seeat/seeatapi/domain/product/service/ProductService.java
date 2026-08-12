package com.seeat.seeatapi.domain.product.service;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.product.dto.request.ProductCreateRequest;
import com.seeat.seeatapi.domain.product.dto.request.ProductFaqCreateRequest;
import com.seeat.seeatapi.domain.product.dto.request.ProductStatusUpdateRequest;
import com.seeat.seeatapi.domain.product.dto.request.ProductUpdateRequest;
import com.seeat.seeatapi.domain.product.dto.response.*;
import com.seeat.seeatapi.domain.product.entity.*;
import com.seeat.seeatapi.domain.product.repository.*;
import com.seeat.seeatapi.global.common.FileStorageService;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final int MAX_IMAGE_COUNT = 5;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductFaqRepository productFaqRepository;
    private final FileStorageService fileStorageService;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository productImageRepository,
            ProductTagRepository productTagRepository,
            ProductFaqRepository productFaqRepository,
            FileStorageService fileStorageService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productTagRepository = productTagRepository;
        this.productFaqRepository = productFaqRepository;
        this.fileStorageService = fileStorageService;
    }

    // 2-1 수산물 상품 등록
    @Transactional
    public ProductCreateResponse createProduct(Member seller, ProductCreateRequest request, List<MultipartFile> images) {
        if (images == null || images.isEmpty() || images.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "존재하지 않는 카테고리입니다."));

        boolean isMandatoryAuction = request.isMandatoryAuction() != null && request.isMandatoryAuction();

        Product product = new Product(
                seller, category, request.name(), request.origin(), request.storageType(),
                request.weight(), request.weightUnit(), isMandatoryAuction,
                request.price(), request.stockQuantity(),
                request.auctionDeadline(), request.description()
        );
        productRepository.save(product);

        List<String> imageUrls = uploadImages(product, images);

        if (request.tags() != null) {
            request.tags().forEach(tagName -> productTagRepository.save(new ProductTag(product, tagName)));
        }

        return ProductCreateResponse.of(product, imageUrls);
    }

    // 2-2 수산물 분류 조회 (트리)
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    // 2-3 상품 상세 조회
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = findProductOrThrow(productId);

        List<ProductImageResponse> images = productImageRepository
                .findByProductProductIdOrderBySortOrderAsc(productId).stream()
                .map(img -> new ProductImageResponse(List.of(img.getImageUrl())))
                .collect(Collectors.toList());

        List<String> tags = productTagRepository.findAll().stream()
                .filter(tag -> tag.getProduct().getProductId().equals(productId))
                .map(ProductTag::getTagName)
                .collect(Collectors.toList());

        return ProductDetailResponse.from(product, images, tags);
    }

    // 2-3 필터 및 정렬 검색
    public PageResponse<ProductSearchResponse> search(
            String category, String origin, BigDecimal priceMin, BigDecimal priceMax,
            String storageType, String sort, Pageable pageable
    ) {
        Page<Product> page = productRepository.search(category, origin, priceMin, priceMax, storageType, sort, pageable);

        Page<ProductSearchResponse> responsePage = page.map(product -> {
            List<String> tags = productTagRepository.findAll().stream()
                    .filter(tag -> tag.getProduct().getProductId().equals(product.getProductId()))
                    .map(ProductTag::getTagName)
                    .collect(Collectors.toList());

            String thumbnailUrl = productImageRepository
                    .findByProductProductIdOrderBySortOrderAsc(product.getProductId())
                    .stream().findFirst().map(ProductImage::getImageUrl).orElse(null);

            return ProductSearchResponse.of(product, tags, thumbnailUrl);
        });

        return PageResponse.of(responsePage);
    }

    // [신규] 판매자 상품 목록 조회 (GET /api/v1/seller/products)
    public PageResponse<ProductSellerListResponse> getSellerProducts(Long sellerId, Pageable pageable) {
        Page<Product> page = productRepository.findBySeller(sellerId, pageable);

        Page<ProductSellerListResponse> responsePage = page.map(product -> {
            String thumbnailUrl = productImageRepository
                    .findByProductProductIdOrderBySortOrderAsc(product.getProductId())
                    .stream().findFirst().map(ProductImage::getImageUrl).orElse(null);

            return ProductSellerListResponse.of(product, thumbnailUrl);
        });

        return PageResponse.of(responsePage);
    }

    // 2-4 상품 문의 등록
    @Transactional
    public void createFaq(Member member, Long productId, ProductFaqCreateRequest request) {
        Product product = findProductOrThrow(productId);
        ProductFaq faq = new ProductFaq(product, member, request.content());
        productFaqRepository.save(faq);
    }

    // 2-4 상품 문의 조회
    public PageResponse<ProductFaqResponse> getFaqs(Long productId, Pageable pageable) {
        Page<ProductFaqResponse> page = productFaqRepository.findByProductProductId(productId, pageable)
                .map(ProductFaqResponse::from);
        return PageResponse.of(page);
    }

    // 2-5 상품 정보 수정
    @Transactional
    public ProductUpdateResponse updateProduct(Long sellerId, Long productId, ProductUpdateRequest request) {
        Product product = findProductOrThrow(productId);
        validateOwnership(product, sellerId);

        product.updateInfo(
                request.name(), request.origin(), request.storageType(),
                request.weight(), request.weightUnit(), request.price(), request.stockQuantity(),
                request.auctionDeadline(), request.description()
        );

        if (request.tags() != null) {
            productTagRepository.deleteByProductProductId(productId);
            request.tags().forEach(tagName -> productTagRepository.save(new ProductTag(product, tagName)));
        }

        return ProductUpdateResponse.from(product);
    }

    // [신규] 판매 상태 변경 (판매중/품절/판매중지)
    @Transactional
    public ProductStatusResponse updateStatus(Long sellerId, Long productId, ProductStatusUpdateRequest request) {
        Product product = findProductOrThrow(productId);
        validateOwnership(product, sellerId);

        product.changeStatus(request.status());

        return new ProductStatusResponse(product.getProductId(), product.getStatus());
    }

    // 2-6 상품 이미지 개별 추가
    @Transactional
    public ProductImageResponse addImages(Long sellerId, Long productId, List<MultipartFile> images) {
        Product product = findProductOrThrow(productId);
        validateOwnership(product, sellerId);

        long existingCount = productImageRepository.countByProductProductId(productId);
        if (existingCount + images.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        List<String> imageUrls = uploadImages(product, images, (int) existingCount);
        return new ProductImageResponse(imageUrls);
    }

    // 2-7 상품 이미지 개별 삭제
    @Transactional
    public void deleteImage(Long sellerId, Long productId, Long imageId) {
        Product product = findProductOrThrow(productId);
        validateOwnership(product, sellerId);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getProduct().getProductId().equals(productId)) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        productImageRepository.delete(image);
    }

    private List<String> uploadImages(Product product, List<MultipartFile> images) {
        return uploadImages(product, images, 0);
    }

    private List<String> uploadImages(Product product, List<MultipartFile> images, int startSortOrder) {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            String url = fileStorageService.upload(images.get(i), "products");
            productImageRepository.save(new ProductImage(product, url, startSortOrder + i));
            urls.add(url);
        }
        return urls;
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void validateOwnership(Product product, Long sellerId) {
        if (!product.isOwnedBy(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}