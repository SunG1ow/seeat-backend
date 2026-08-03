package com.seeat.seeatapi.domain.member.service;

import com.seeat.seeatapi.domain.member.dto.MemberMapper;
import com.seeat.seeatapi.domain.member.dto.request.*;
import com.seeat.seeatapi.domain.member.dto.response.AddressResponse;
import com.seeat.seeatapi.domain.member.dto.response.MemberProfileResponse;
import com.seeat.seeatapi.domain.member.dto.response.WithdrawResponse;
import com.seeat.seeatapi.domain.member.entity.DeliveryAddress;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.repository.DeliveryAddressRepository;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private static final int MAX_ADDRESS_COUNT = 5;

    private final MemberRepository memberRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

    public MemberService(
            MemberRepository memberRepository,
            DeliveryAddressRepository deliveryAddressRepository,
            PasswordEncoder passwordEncoder,
            MemberMapper memberMapper
    ) {
        this.memberRepository = memberRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberMapper = memberMapper;
    }

    // 5-1 프로필 조회
    public MemberProfileResponse getProfile(Long userId) {
        Member member = findMemberOrThrow(userId);
        return memberMapper.toProfileResponse(member);
    }

    // 5-1 프로필 수정 (profileImageUrl은 24단계 Controller에서 S3 업로드 후 전달 예정)
    @Transactional
    public MemberProfileResponse updateProfile(Long userId, UpdateProfileRequest request, String profileImageUrl) {
        Member member = findMemberOrThrow(userId);
        member.updateProfile(request.nickname(), request.phoneNumber(), profileImageUrl);
        return memberMapper.toProfileResponse(member);
    }

    // 5-5 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Member member = findMemberOrThrow(userId);

        if (!passwordEncoder.matches(request.currentPassword(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    // 5-2 배송지 목록 조회
    public List<AddressResponse> getAddresses(Long userId) {
        return deliveryAddressRepository.findByMemberUserId(userId).stream()
                .map(AddressResponse::from)
                .collect(Collectors.toList());
    }

    // 5-2 배송지 추가 (최대 5개 제한)
    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        Member member = findMemberOrThrow(userId);

        long currentCount = deliveryAddressRepository.countByMemberUserId(userId);
        if (currentCount >= MAX_ADDRESS_COUNT) {
            throw new BusinessException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }

        boolean isDefault = request.isDefault() != null && request.isDefault();

        DeliveryAddress address = new DeliveryAddress(
                member, request.alias(), request.receiverName(),
                request.receiverPhone(), request.address(), isDefault
        );
        deliveryAddressRepository.save(address);

        return AddressResponse.from(address);
    }

    // 5-2 배송지 삭제
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getMember().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        deliveryAddressRepository.delete(address);
    }

    // 5-4 회원 탈퇴
    @Transactional
    public WithdrawResponse withdraw(Long userId, WithdrawRequest request) {
        Member member = findMemberOrThrow(userId);

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        member.withdraw();
        return WithdrawResponse.from(member);
    }

    private Member findMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}