package com.seeat.seeatapi.domain.notification.repository;

import com.seeat.seeatapi.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByMemberUserId(Long userId, Pageable pageable);
}