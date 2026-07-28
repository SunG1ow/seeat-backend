package com.seeat.seeatapi.domain.member.dto.response;

import com.seeat.seeatapi.domain.member.entity.DeliveryAddress;

// 5-2 배송지 응답
public record AddressResponse(
        Long addressId,
        String alias,
        String receiverName,
        String receiverPhone,
        String address,
        boolean isDefault
) {
    public static AddressResponse from(DeliveryAddress address) {
        return new AddressResponse(
                address.getAddressId(),
                address.getAlias(),
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getAddress(),
                address.isDefault()
        );
    }
}