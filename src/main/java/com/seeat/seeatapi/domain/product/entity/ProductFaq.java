package com.seeat.seeatapi.domain.product.entity;

import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.global.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_faq")
public class ProductFaq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faq_id")
    private Long faqId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 500)
    private String content;

    protected ProductFaq() {
    }

    public ProductFaq(Product product, Member member, String content) {
        this.product = product;
        this.member = member;
        this.content = content;
    }

    public Long getFaqId() {
        return faqId;
    }

    public Product getProduct() {
        return product;
    }

    public Member getMember() {
        return member;
    }

    public String getContent() {
        return content;
    }
}