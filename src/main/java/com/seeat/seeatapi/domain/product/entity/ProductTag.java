package com.seeat.seeatapi.domain.product.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_tag")
public class ProductTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "tag_name", nullable = false, length = 30)
    private String tagName;

    protected ProductTag() {
    }

    public ProductTag(Product product, String tagName) {
        this.product = product;
        this.tagName = tagName;
    }

    public Long getTagId() {
        return tagId;
    }

    public Product getProduct() {
        return product;
    }

    public String getTagName() {
        return tagName;
    }
}