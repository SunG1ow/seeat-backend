package com.seeat.seeatapi.domain.member.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_address")
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 30)
    private String alias;

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    protected DeliveryAddress() {
    }

    // 5-2 배송지 추가
    public DeliveryAddress(Member member, String alias, String receiverName,
                           String receiverPhone, String address, boolean isDefault) {
        this.member = member;
        this.alias = alias;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.address = address;
        this.isDefault = isDefault;
    }

    public Long getAddressId() {
        return addressId;
    }

    public Member getMember() {
        return member;
    }

    public String getAlias() {
        return alias;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getAddress() {
        return address;
    }

    public boolean isDefault() {
        return isDefault;
    }
}