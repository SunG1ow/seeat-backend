package com.seeat.seeatapi.domain.product.service;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import com.seeat.seeatapi.domain.product.dto.request.ProductCreateRequest;
import com.seeat.seeatapi.domain.product.dto.response.ProductCreateResponse;
import com.seeat.seeatapi.domain.product.entity.Category;
import com.seeat.seeatapi.domain.product.repository.*;
import com.seeat.seeatapi.global.common.FileStorageService;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductTagRepository productTagRepository;
    @Mock private ProductFaqRepository productFaqRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("2-1 상품 등록 성공")
    void createProduct_success() {
        // given
        Member seller = new Member("seller@seeat.com", "encoded", MemberRole.SELLER, "판매자", "010-1111-2222");
        Category category = new Category("어류", null);
        ProductCreateRequest request = new ProductCreateRequest(
                1L, "완도산 활전복", "완도", "냉장", BigDecimal.valueOf(1.5), "kg",
                false, BigDecimal.valueOf(32000), 10,
                LocalDateTime.now().plusDays(3), "완도산 활전복입니다.",
                List.of("활어", "산지직송")
        );
        MultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test-data".getBytes());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(fileStorageService.upload(any(), any())).thenReturn("https://cdn.seeat.com/products/test.jpg");

        // when
        ProductCreateResponse response = productService.createProduct(seller, request, List.of(image));

        // then
        assertThat(response.status()).isEqualTo("PENDING_REVIEW");
        assertThat(response.imageUrls()).hasSize(1);
        verify(productRepository).save(any());
    }

    @Test
    @DisplayName("2-1 상품 등록 실패 - 이미지 6장 초과")
    void createProduct_fail_tooManyImages() {
        // given
        Member seller = new Member("seller@seeat.com", "encoded", MemberRole.SELLER, "판매자", "010-1111-2222");
        ProductCreateRequest request = new ProductCreateRequest(
                1L, "완도산 활전복", "완도", "냉장", null, null, false,
                BigDecimal.valueOf(32000), 10,
                null, null,
                null
        );
        List<MultipartFile> sixImages = List.of(
                new MockMultipartFile("images", "1.jpg", "image/jpeg", "a".getBytes()),
                new MockMultipartFile("images", "2.jpg", "image/jpeg", "b".getBytes()),
                new MockMultipartFile("images", "3.jpg", "image/jpeg", "c".getBytes()),
                new MockMultipartFile("images", "4.jpg", "image/jpeg", "d".getBytes()),
                new MockMultipartFile("images", "5.jpg", "image/jpeg", "e".getBytes()),
                new MockMultipartFile("images", "6.jpg", "image/jpeg", "f".getBytes())
        );

        // when & then
        assertThatThrownBy(() -> productService.createProduct(seller, request, sixImages))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_LIMIT_EXCEEDED);
    }
}