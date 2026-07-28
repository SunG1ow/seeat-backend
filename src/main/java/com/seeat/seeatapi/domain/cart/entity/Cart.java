package com.seeat.seeatapi.domain.cart.entity;

import com.seeat.seeatapi.domain.member.entity.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Member member;

    protected Cart() {
    }

    public Cart(Member member) {
        this.member = member;
    }

    public Long getCartId() {
        return cartId;
    }

    public Member getMember() {
        return member;
    }
}