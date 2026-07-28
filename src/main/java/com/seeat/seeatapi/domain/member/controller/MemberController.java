package com.seeat.seeatapi.domain.member.controller;

import com.seeat.seeatapi.domain.member.dto.request.*;
import com.seeat.seeatapi.domain.member.dto.response.AddressResponse;
import com.seeat.seeatapi.domain.member.dto.response.MemberProfileResponse;
import com.seeat.seeatapi.domain.member.service.MemberService;
import com.seeat.seeatapi.global.response.ApiResponse;
import com.seeat.seeatapi.global.security.CurrentMemberId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 5-1 프로필 정보 조회
    @GetMapping
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getProfile(memberId)));
    }

    // 5-1 프로필 정보 수정
    @PutMapping
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateProfile(
            @CurrentMemberId Long memberId,
            @ModelAttribute UpdateProfileRequest request,
            @RequestParam(required = false) MultipartFile profileImage
    ) {
        String profileImageUrl = null; // TODO: FileStorageService로 업로드 후 URL 대입 (24단계 상품 컨트롤러와 동일 패턴)
        MemberProfileResponse response = memberService.updateProfile(memberId, request, profileImageUrl);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 5-5 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        memberService.changePassword(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다."));
    }

    // 5-2 배송지 목록 조회
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getAddresses(memberId)));
    }

    // 5-2 배송지 추가
    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = memberService.addAddress(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 5-2 배송지 삭제
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Object>> deleteAddress(
            @CurrentMemberId Long memberId,
            @PathVariable Long addressId
    ) {
        memberService.deleteAddress(memberId, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "배송지가 삭제되었습니다."));
    }

    // 5-4 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<ApiResponse<Object>> withdraw(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        memberService.withdraw(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "회원 탈퇴가 완료되었습니다."));
    }
}