# Seeat API 테스트 중 발견된 문제 정리 (2026-08-03)

`feature/api` 브랜치 기준으로 Postman 전체 테스트(34개 요청)를 돌려서 확인한 결과입니다. OrderController 라우팅 수정 감사합니다 — 4-1/4-2/4-4/4-5 전부 정상 통과 확인했습니다. 아래는 그 외 남은 문제들입니다.

---

## 1. 관리자 로그인 401 (`POST /api/v1/admin/login`)

계정 정보는 확인 주신 대로 정확히 사용 중입니다 (`admin01@seeat.com` / `adminPassword1!`). 말씀 주신 대로 DB에 계정 자체가 없어서 그런 것 같습니다. Railway DB(`seafood_dummy_test`)에 admin01 계정 생성 부탁드립니다.

추가로, OTP 코드도 저희 쪽에서는 임시로 `"123456"` 고정값을 보내고 있는데, 실제 OTP 검증 방식이 어떻게 되는지(고정 테스트 코드를 허용하는지, 실시간 TOTP가 필요한지) 알려주시면 반영하겠습니다.

---

## 2. 사업자 인증 409 Conflict (`POST /api/v1/auth/verify-business`)

409로 응답이 바뀐 거 확인했습니다 — 중복 사업자등록번호 검증 추가하신 것 같아요, 감사합니다. 저희 테스트 바디에 있는 사업자등록번호가 고정값이라 최초 1회 이후로는 계속 충돌 나는 거라 저희 쪽 테스트 데이터 문제입니다. 별도 조치 필요 없습니다.

---

## 3. 배송 조회 500 오류 (`GET /api/v1/users/me/delivery`)

**파일**: `DeliveryRepository.java` (15~19행)

```java
@Query(
    "SELECT d FROM Delivery d WHERE d.order.buyer.userId = :userId " +
    "AND (:startDate IS NULL OR d.order.createdAt >= :startDate) " +
    "AND (:endDate IS NULL OR d.order.createdAt <= :endDate)"
)
Page<Delivery> findByBuyerAndPeriod(
    @Param("userId") Long userId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate,
    Pageable pageable
);
```

`Order.createdAt`은 `LocalDateTime` 타입인데 여기서 비교하는 `startDate`/`endDate`는 `LocalDate` 타입이라 타입이 안 맞습니다. 그래서 쿼리 실행할 때 에러가 나고 500으로 응답되는 것 같습니다.

**요청 예시**: `GET /api/v1/users/me/delivery?startDate=2026-07-01&endDate=2026-07-24&page=0&size=20`

**수정 제안**: 서비스 계층(`DeliveryService.getMyDeliveries`)에서 `LocalDate`를 `LocalDateTime`으로 변환해서 넘기시면 될 것 같습니다 (예: `startDate.atStartOfDay()`, `endDate.atTime(23,59,59)`).

---

## 4. 회원 탈퇴 500 오류 (`DELETE /api/v1/users/me`)

**파일**: `MemberController.java` (83~88행)

```java
@PostMapping("/withdraw") // 또는 @DeleteMapping
public ResponseEntity<Void> withdraw(@AuthenticationPrincipal Long memberId, @RequestBody WithdrawRequest request) {
    memberService.withdraw(memberId, request);
    return ResponseEntity.ok().build();
}
```

두 가지가 안 맞는 것 같습니다.

1. 실제 등록된 경로가 `POST /api/v1/users/me/withdraw`인데, 저희는 명세서 기준으로 `DELETE /api/v1/users/me`를 호출하고 있어서 매칭이 안 됩니다.
2. 이 메서드만 `@AuthenticationPrincipal`을 쓰고 있는데, 같은 컨트롤러의 다른 메서드들(`getProfile`, `updateProfile` 등)은 전부 `@CurrentMemberId`를 쓰고 있어서, 경로를 고치더라도 이 부분도 같이 맞춰야 할 것 같습니다.

**수정 제안**: `@PostMapping("/withdraw")` → `@DeleteMapping`(경로 없이)로, `@AuthenticationPrincipal Long memberId` → `@CurrentMemberId Long memberId`로 변경.

참고로 8/1일 테스트 때는 이 부분이 정상 통과였어서, 최근 커밋에서 새로 깨진 것 같습니다.

---

## 요약

| 항목 | 상태 | 필요 조치 |
|---|---|---|
| 관리자 로그인 401 | 미해결 | DB에 admin01 계정 생성 필요, OTP 방식 확인 필요 |
| 사업자 인증 409 | 정상 (테스트 데이터 문제일 뿐) | 조치 불필요 |
| 배송 조회 500 | 미해결 | `DeliveryRepository` 쿼리 타입 불일치 수정 필요 |
| 회원 탈퇴 500 | 미해결 (회귀로 추정) | `MemberController.withdraw` 경로/애노테이션 수정 필요 |
