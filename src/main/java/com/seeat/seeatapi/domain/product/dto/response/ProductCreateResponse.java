package com.seeat.seeatapi.domain.product.dto.response;

import com.seeat.seeatapi.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateResponse {

    private Long productId;
    private String status;
    private List<String> imageUrls;

    public static ProductCreateResponse of(Product product, List<String> imageUrls) {
        return ProductCreateResponse.builder()
                .productId(product.getProductId())
                .status(product.getStatus().name())
                .imageUrls(imageUrls)
                .build();
    }
}