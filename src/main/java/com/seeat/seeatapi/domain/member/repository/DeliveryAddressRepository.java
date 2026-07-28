package com.seeat.seeatapi.domain.member.repository;

import com.seeat.seeatapi.domain.member.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    List<DeliveryAddress> findByMemberUserId(Long userId);
    long countByMemberUserId(Long userId); // 5-2 최대 5개 제한 검증용
}