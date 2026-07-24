package com.seeat.seeatapi.domain.delivery.entity;

import com.seeat.seeatapi.domain.order.entity.Order;
import jakarta.persistence.*;

@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(length = 50)
    private String carrier;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    protected Delivery() {
    }

    public Delivery(Order order) {
        this.order = order;
    }

    // 4-3 SHIPPING 전환 시 택배사/운송장번호 upsert
    public void updateTrackingInfo(String carrier, String trackingNumber) {
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public Order getOrder() {
        return order;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}