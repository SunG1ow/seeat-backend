package com.seeat.seeatapi.domain.cart.repository;

import com.seeat.seeatapi.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMemberUserId(Long userId);
}