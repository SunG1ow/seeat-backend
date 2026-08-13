package com.seeat.seeatapi.domain.member.repository;

import com.seeat.seeatapi.domain.member.entity.SellerBusinessInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerBusinessInfoRepository extends JpaRepository<SellerBusinessInfo, Long> {
    Optional<SellerBusinessInfo> findByUserId(Long userId);
    Optional<SellerBusinessInfo> findByBusinessRegistrationNumber(String businessRegistrationNumber);
}