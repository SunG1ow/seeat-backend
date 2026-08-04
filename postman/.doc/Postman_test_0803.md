# Seeat API 테스트 작업 상세 인수인계 (AI 세션용)

작성 시점: 2026-08-03. 이전 인수인계 메모(`인수인계_메모.md`, 2026-08-01 작성) 이후 진행된 내용을 반영한 최신 버전입니다. 새 대화창에서 이어받는 AI는 이 문서 하나만 읽으면 전체 맥락을 파악할 수 있도록 작성했습니다.

역할 주의: 사용자(SunG1ow)는 QA 담당입니다. **백엔드 코드는 절대 직접 수정하지 마세요.** 문제를 코드 레벨까지 정확히 분석하고 원인/증거/제안 수정 방향을 제시하는 것까지가 역할이고, 실제 수정과 커밋/푸시는 API 담당자(위성훈, GitHub: wesunghun)가 합니다. Postman 컬렉션/환경 쪽 수정(테스트 스크립트, 토큰 변수, 요청 바디 등)은 지금까지 사용자 승인 하에 진행해왔습니다 — 이것도 매번 사용자에게 먼저 물어보고 진행할 것.

---

## 1. 프로젝트 구조 및 환경

- 백엔드 로컬 클론: `C:\Users\ChoSB\Desktop\Develop\seeat-api` (bash에서는 `/sessions/.../mnt/seeat-api`)
- 작업 워크스페이스(Postman 문서 등 저장 위치): `C:\Users\ChoSB\Desktop\Develop\Seeat`
- 로컬 서버 포트: 8081 (`application.yml` / `application-local.yml`)
- 로컬 서버는 **Railway MySQL(`seafood_dummy_test` DB)에 직접 연결**해서 돌아감 (H2 아님). 연결 정보는 `.vscode/settings.json`의 `terminal.integrated.env.windows`에 설정되어 있음 (JAVA_HOME, SPRING_DATASOURCE_URL/USERNAME/PASSWORD).
- `gradlew.bat build`는 테스트 단계에서 H2/Railway 드라이버 충돌로 실패할 수 있음 → `gradlew.bat build -x test`로 우회 (또는 `gradlew.bat clean build -x test`).

### Git / 브랜치 (★ 2026-08-03 핵심 변경 사항)

- origin: `https://github.com/wesunghun/seeat-api.git`
- **로컬 저장소는 기존에 `main` 브랜치를 보고 있었으나, 2026-08-03에 `feature/api` 브랜치로 전환함.** 실제 버그 수정(OrderController 라우팅 등)이 `main`이 아니라 `feature/api`에만 커밋되어 있었기 때문.
- 전환 후 로컬 HEAD: `feature/api` @ `2f81f36` ("Ordercontroller 라우팅 버그 커밋", `[origin/feature/api]`와 동기화됨)
- `feature/api` 최근 커밋 로그(위에서부터 최신순):
  - `2f81f36` Ordercontroller 라우팅 버그 커밋
  - `7ddcb03` Merge branch 'feature/api' of https://github.com/wesunghun/seeat-api into feature/api
  - `2da87f5` fix: 잘못된 패키지 선언 수정 (JpaAuditingConfig, QuerydslConfig)
  - `62a209a` Merge pull request #9 from wesunghun/feature/api
  - `1cfdfef` fix: Postman 테스트로 발견된 API 응답/보안 버그 수정 ← **5-4 회원탈퇴 라우팅 회귀가 이 커밋 근처에서 발생한 것으로 추정 (미확인, diff로 특정 안 함)**
- **다음 세션에서 주의**: 만약 새로 fork를 pull하거나 다른 브랜치로 체크아웃하게 되면, `git branch -vv`로 현재 브랜치를 반드시 먼저 확인할 것. 지금까지 겪은 문제 중 상당수가 "고쳐졌다는 얘기를 들었는데 재현된다"는 게 실은 브랜치 불일치였음 (아래 6-D 참고).

### 반복적으로 재발했던 이슈 (현재 상태 포함)

1. **JpaAuditingConfig.java / QuerydslConfig.java 패키지 선언 오류** — `com.seeat.seeatapi.global.config`로 잘못 선언되어 있었음(실제 위치는 `.../seeatapi/config/`). **`feature/api`에서는 커밋 `2da87f5`로 이미 수정 완료됨** (GitHub raw로 직접 확인함). `main` 브랜치를 다시 보게 되면 이 문제가 재발할 수 있음.
2. **`.vscode/settings.json`** — 원래 없었고 `.github/settings.json`에 잘못 들어있던 적 있음. 지금은 `.vscode/settings.json`을 만들어뒀음. 브랜치 전환/재클론 시 재확인 필요. `.github/settings.json`은 untracked로 남아있을 수 있음(안전하게 삭제 가능, 수동 삭제 필요).
3. **CRLF/LF 줄바꿈 노이즈 (2026-08-03 신규 확인, 실제 버그 아님)** — 로컬(Windows)에서 `git status`를 찍으면 저장소 안 거의 전체 파일이 "modified"로 표시됨. `git diff --stat`로 확인해보면 예를 들어 `OrderController.java`가 "139 insertions, 139 deletions"로 나오는데 실제로는 내용이 한 글자도 안 바뀌고 줄바꿈 문자만 다름 (`core.autocrlf` 미설정, `.gitattributes` 없음). **이건 실제 코드 문제가 아니므로 무시해도 됨.** 단, API 담당자가 "롤백해서 비교했는데 파일 변경사항이 없다"고 말하는 원인이 이 노이즈일 가능성이 있으니, 실제 버그 유무는 항상 `git diff`가 아니라 **GitHub raw 파일을 직접 fetch해서(`https://raw.githubusercontent.com/wesunghun/seeat-api/<branch>/<path>`) 코드 내용 자체를 읽어서 판단**할 것. 이 방법이 이번 세션에서 가장 신뢰할 수 있는 확인 수단이었음.

---

## 2. Postman 리소스 (전부 사본 작업 중, 원본 안 건드림)

- 원본 워크스페이스 "Seeat Project" (건드리지 않음, id `fd6c3557-f37e-4bff-9431-41fc4d16e840`)
- **작업용 워크스페이스**: "Seeat Project - Dummy Data" (id `4a7018c7-3b1e-444a-8bd7-5667754af171`)
- **작업용 컬렉션**: "SEEAT API v1.1.4 Copy" (id `6a659ece-35de-490e-9501-6b0c13040254`, owner `56842577`). Postman MCP 도구 호출 시 `collectionId`는 `56842577-6a659ece-35de-490e-9501-6b0c13040254` 형식(OWNER-UUID) 사용.
- **작업용 환경**: "Seeat Local - Dummy Data" (id `5e4837c5-8f42-4850-a19c-5fc264c4615c`)
- 요청/바디/현재 상태 전체 목록과 사람이 읽기 쉬운 정리: `Seeat` 폴더의 `Postman_test_0801-2.md` (파일명은 이거지만 내용상 "Seeat_Postman_테스트_정리" 역할, 2026-08-02에 업데이트됨, 팀 공유용으로도 사용 중)
- 이번(2026-08-03) 신규 정리 문서: `Seeat_API_문제점_요약_API담당자용_0803.md` (API 담당자 전달용, 이 문서와 같이 생성)

### Postman CLI로 로컬 실행하는 법
```
postman login --with-api-key <API키>
postman collection run 56842577-6a659ece-35de-490e-9501-6b0c13040254 -e 56842577-5e4837c5-8f42-4850-a19c-5fc264c4615c
```
CLI는 사용자 PC(Windows)에 설치됨.

### Postman MCP 도구 사용 메모
- `getCollection` (model=full)로 컬렉션 전체를 가져오면 응답이 커서(60KB+) `<persisted-output>`로 파일에 저장됨. bash에서 `find / -iname "toolu_...json"`으로 찾아서 python으로 파싱하는 식으로 다뤘음. 경로는 `/sessions/.../mnt/.claude/projects/.../tool-results/toolu_XXXX.json` 형태.
- `updateCollectionRequest`는 폴더 이동/순서 변경은 지원 안 함 (`This endpoint does not support changing the folder of a request`). 폴더 내 순서를 바꾸려면 `putCollection`으로 컬렉션 전체(모든 item의 `id` 보존)를 통째로 교체해야 하는데, 32개 요청짜리 컬렉션을 손으로 재구성하는 건 실수 위험이 커서 이번 세션에서는 **회피**하고 대신 pre-request 스크립트로 순서 의존성을 없애는 방식을 택함 (아래 3-C 참고).

---

## 3. 이번 세션(2026-08-02~03)에서 실제로 적용한 Postman 변경 사항

### A. 1-4 판매자 사업자 인증 (`requestId`: `700ef751-1bf1-4102-8816-c784e4c9d843`)
- **인증 토큰 변경**: `{{buyerAccessToken}}` → `{{sellerAccessToken}}` (사업자 인증은 SELLER 권한이 필요한데 BUYER 토큰으로 잘못 걸려 있었음)
- **pre-request 스크립트 추가**: `sellerAccessToken`이 비어있으면 1-2b와 동일한 방식(자체 회원가입 role=SELLER → 로그인)으로 채워넣음. 폴더 실행 순서(1-4가 1-2b보다 먼저 옴)와 무관하게 항상 유효한 SELLER 토큰으로 호출되도록 함.
- 적용 결과: 토큰 문제는 해결됨. 이후 재테스트에서 500(다른 원인, 아래 4-C 참고) → 409(백엔드가 `feature/api`에서 수정된 뒤)로 변화 확인.

이 외에 이전 세션(2026-08-01)에 이미 적용되어 있던 것들(토큰 분리 buyer/seller/admin/temp, 2-1 바디 채움, 3-3/7-2 테스트 스크립트 수정 등)은 `Postman_test_0801-2.md`에 정리되어 있으므로 여기서는 생략.

---

## 4. 발견된 문제 전체 목록 (코드 레벨 근거 포함)

### A. OrderController 라우팅 버그 — ✅ 해결 확인됨 (2026-08-03)

- **증상**: 4-1(주문서 작성), 4-2(결제), 4-4(상태이력), 4-5(구매내역), 4-6(배송추적) 전부 500.
- **원인** (`main` 브랜치 `OrderController.java`): 클래스 레벨 `@RequestMapping("/api/orders")` + 각 메서드가 절대경로(`/api/v1/orders` 등)를 또 사용 → Spring이 이어붙여서 실제 등록 경로가 `/api/orders/api/v1/orders`처럼 됨. Postman이 호출하는 `/api/v1/orders`와 매칭 안 됨.
- **500으로 보이는 이유**: `GlobalExceptionHandler.java` 42~47행에 `@ExceptionHandler(Exception.class)` catch-all이 있어서, 원래 404여야 할 라우팅 미스매치가 뭉뚱그려져 500으로 나옴.
- **단순 제거가 위험했던 이유**: `OrderController` 안에 4-3(관리자 상태변경)과 완전히 동일한 `changeStatus` 메서드가 중복으로 있었고, 이미 `AdminOrderController`가 같은 경로(`PATCH /api/v1/orders/{orderId}/status`)를 정상 처리 중이었음. 클래스 레벨 매핑만 지우면 두 컨트롤러가 경로가 겹쳐서 서버 기동 자체가 실패(Ambiguous mapping)함.
- **`feature/api`에서의 수정 내용** (GitHub raw로 직접 확인): 클래스 레벨 `@RequestMapping` 제거 + 중복 `changeStatus` 메서드 완전 삭제. 정확히 우리가 제안했던 안전한 수정 방향과 일치.
- **재테스트 결과**: 4-1/4-2/4-4/4-5 전부 통과(201/200) 확인. **해결됨.**
- 참고: 4-3은 이 버그와 무관 — `AdminOrderController`가 처음부터 정상 처리 중이었고, 1-3(관리자 로그인) 실패로 `adminAccessToken`이 없어서 401이 나는 것뿐. 1-3 해결 시 같이 해결될 것으로 예상.

### B. 1-3 관리자 로그인 401 — ❌ 미해결

- **증상**: `POST /api/v1/admin/login` → 401.
- **Postman 바디**: `{"email": "{{adminEmail}}", "password": "{{adminPassword}}", "otpCode": "123456"}`
- **환경변수 확인 결과** (2026-08-02, `getEnvironment`로 직접 조회): `adminEmail` = `admin01@seeat.com`, `adminPassword` = `adminPassword1!` — 이미 API 담당자가 알려준 실제 계정과 정확히 일치. 즉 **Postman 쪽 설정 문제가 아님.**
- **의심 지점**: `otpCode`가 `"123456"` 고정값. 실제 TOTP라면 30초마다 값이 바뀌므로 고정값은 항상 틀릴 가능성이 큼.
- **API 담당자(위성훈) 확인 내용** (2026-08-02, 카카오톡/슬랙 스크린샷): "관리자 로그인은 해당 계정 자체를 db에 작성해야 실행이 되는 구조", "연결 DB 확인 필요 (Railway vs 로컬) 후 계정 재생성" — 즉 **admin01 계정이 우리가 붙어있는 Railway DB에 아예 없을 가능성이 높다는 뜻**. OTP 문제 이전에 계정 존재 여부가 우선 확인 대상.
- **상태**: API 담당자가 DB에 계정을 만들어줄 때까지 대기. 코드 버그인지 데이터 문제인지는 계정이 생성된 후 재확인 필요.
- **AuthService.adminLogin()의 코드 특성** (읽었으나 세부 라인은 인용 안 함): INVALID_CREDENTIALS(계정 없음/role 불일치/비번 틀림)와 INVALID_OTP(시크릿키 없음/OTP 코드 불일치)가 둘 다 401로 응답되어 있어서, 응답만으로는 원인 구분이 안 되는 구조. 이 부분도 API 담당자에게 "에러 코드를 구분해서 응답해달라"고 요청할 만한 개선 포인트.

### C. 1-4 사업자 인증 UNIQUE 제약 — 🟡 부분 해결 (백엔드는 개선됨, 테스트 데이터 문제 남음)

- **1차 증상 (Postman 문제, 해결함)**: 인증 토큰이 `{{buyerAccessToken}}`으로 잘못 걸려 있었음 → 위 3-A에서 수정.
- **2차 증상 (백엔드 문제, 코드로 원인 특정함)**: 토큰을 고쳐도 500 재현. 원인:
  - `SellerBusinessInfo.java` 21행: `business_registration_number` 컬럼이 `unique = true`.
  - Postman 바디의 `businessRegistrationNumber`가 `"1234567890"` 고정값.
  - `AuthService.verifyBusiness()` (당시 96~127행)에 "이 번호가 다른 유저에게 이미 등록돼 있는지" 사전 검증 로직이 없어서, 이미 사용된 고정값으로 재인증 시도 시 DB UNIQUE 제약 위반 예외가 그대로 터지고 `GlobalExceptionHandler`의 catch-all이 이를 500으로 응답.
- **`feature/api`로 전환 후 재테스트**: 500 → **409 Conflict**로 바뀜. 명시적인 중복 검증/에러코드가 추가된 것으로 추정됨 (API 담당자가 손본 것으로 보이나, 정확한 커밋은 diff로 특정하지 않았음 — 필요시 `AuthService.java`를 다시 읽어서 확인할 것).
- **남은 문제 (테스트 설계 문제, 백엔드 버그 아님)**: Postman 바디의 `businessRegistrationNumber`가 여전히 고정값이라, 최초 1회 성공 이후로는 실행할 때마다 "합당한" 409가 발생함. 성공 케이스를 반복 검증하려면 이메일처럼 매 실행마다 랜덤 생성하도록 pre-request 스크립트를 고쳐야 함. **사용자가 "추가 수정 없이 현재 상태로 종료"를 선택해서 미적용 상태로 둠.** 필요시 진행 가능.

### D. 4-6 주문 내역 및 배송 추적 — ❌ 신규 발견, 미해결

- **증상**: `GET /api/v1/users/me/delivery?startDate=...&endDate=...` → 500. (OrderController 버그가 해결되기 전까지는 이 버그가 가려져 있었음 — 라우팅 자체가 안 맞아서 이 코드까지 도달하지 못했었음.)
- **원인** (`DeliveryRepository.java` 15~19행):
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
  `Order.createdAt`은 `BaseEntity.java` 17행에서 `LocalDateTime` 타입인데, 비교 대상 파라미터 `startDate`/`endDate`는 `LocalDate` 타입. JPQL에서 `LocalDateTime` 필드를 `LocalDate` 파라미터와 직접 비교하는 타입 불일치로 런타임에 예외가 발생하는 것으로 추정 (Hibernate 파라미터 바인딩/타입 변환 오류). 이 예외 역시 `GlobalExceptionHandler`의 catch-all에 걸려 500으로 응답됨.
- **제안 수정 방향 (구현은 안 함, API 담당자 몫)**: 서비스 계층(`DeliveryService.getMyDeliveries`, 38행 부근)에서 `LocalDate`를 `LocalDateTime`으로 변환해서 넘기는 방법(`startDate.atStartOfDay()`, `endDate.atTime(23,59,59)` 등) 또는 JPQL에서 `d.order.createdAt` 날짜 부분만 비교하도록 캐스팅하는 방법.
- **상태**: 미확인/미수정. 다음 세션 또는 API 담당자에게 전달 필요.

### E. 5-4 회원 탈퇴 — ❌ 신규 발견, 회귀(regression)로 추정, 미해결

- **증상**: `DELETE /api/v1/users/me` → 500. **2026-08-01 테스트 시점에는 정상 통과였음** (`Postman_test_0801-2.md` 참고) — 즉 최근에 새로 깨진 것으로 보임. `feature/api`의 커밋 `1cfdfef`("fix: Postman 테스트로 발견된 API 응답/보안 버그 수정") 부근에서 발생했을 가능성이 있으나 diff로 확정하지는 않았음.
- **원인** (`MemberController.java` 83~88행):
  ```java
  // 5-4 회원 탈퇴
  @PostMapping("/withdraw") // 또는 @DeleteMapping
  public ResponseEntity<Void> withdraw(@AuthenticationPrincipal Long memberId, @RequestBody WithdrawRequest request) {
      memberService.withdraw(memberId, request);
      return ResponseEntity.ok().build();
  }
  ```
  두 가지 문제가 겹쳐 있음:
  1. 클래스 레벨이 `@RequestMapping("/api/v1/users/me")`(20행)인데 메서드가 `@PostMapping("/withdraw")`라서 실제 등록 경로는 `POST /api/v1/users/me/withdraw`. Postman은 `DELETE /api/v1/users/me`를 호출하므로 경로/메서드 둘 다 안 맞아 매칭 자체가 안 됨 (A번 OrderController와 같은 클래스의 버그).
  2. 같은 컨트롤러의 다른 모든 메서드(`getProfile`, `updateProfile`, `getAddresses` 등)는 커스텀 애노테이션 `@CurrentMemberId`를 쓰는데, 이 메서드만 Spring Security 표준 `@AuthenticationPrincipal`을 쓰고 있음. 이 프로젝트의 인증 방식이 `@CurrentMemberId`(커스텀 리졸버, `CurrentMemberIdArgumentResolver.java`)를 통해 memberId를 주입하는 구조로 보이므로, 설령 경로가 맞았어도 `@AuthenticationPrincipal`로 `memberId`가 제대로 채워지지 않았을 가능성이 큼.
  - 코드에 남아있는 주석 `// 또는 @DeleteMapping`이 작업 중 결정을 못 내리고 남겨둔 상태임을 시사함.
- **제안 수정 방향 (구현은 안 함)**: `@PostMapping("/withdraw")` → `@DeleteMapping`(경로 없이, 클래스 레벨 경로 그대로 사용)으로 변경 + `@AuthenticationPrincipal Long memberId` → `@CurrentMemberId Long memberId`로 변경하여 다른 메서드들과 일관성을 맞춤.
- **상태**: 미확인/미수정. API 담당자에게 회귀 가능성과 함께 전달 필요.

### F. 기타 관찰 사항 (버그는 아니고 참고용)

- 마지막 재테스트(2026-08-03) 중 `3-2. 장바구니 상품 추가` 응답 시간이 28.2초로 유독 길게 나온 적이 있음(1회성일 수 있음, 재현되는지 다음 실행에서 확인해볼 만함). 통과는 했음.
- `GlobalExceptionHandler`의 `@ExceptionHandler(Exception.class)` catch-all 자체가 구조적으로 "원인이 다른 여러 문제(404 라우팅 미스매치, 409 제약 위반, 그 외 예상 못한 예외)를 전부 500 하나로 뭉개는" 문제를 안고 있음. A/D/E 세 가지 문제를 진단하는 데 모두 이게 걸림돌이 됐음. 예외 유형별 세분화된 처리(`NoHandlerFoundException` → 404, `DataIntegrityViolationException` → 409 등)를 권장한다고 API 담당자에게 이미 언급함.

---

## 5. 최신 테스트 실행 결과 요약 (2026-08-03, `feature/api` 전환 후)

Postman CLI 실행 (`postman collection run 56842577-6a659ece-35de-490e-9501-6b0c13040254 -e 56842577-5e4837c5-8f42-4850-a19c-5fc264c4615c`) 기준.

- requests: 34/34 실행, iterations 1/1
- **실패한 요청 5개**: 1-3(401), 1-4(409, 위 4-C 참고), 4-3(401, 1-3 연쇄), 4-6(500, 신규), 5-4(500, 신규)
- **나머지 29개 요청은 전부 정상 통과** (1-1, 1-2a, 1-2b, 1-1b, 2-1, 2-2, 2-3, 2-4a, 2-4b, 3-1, 3-2, 3-3, 4-1, 4-2, 4-4, 4-5, 5-1a, 5-1b, 5-2a, 5-2b, 5-2c, 5-3, 6-1, 7-1, 7-2)

이전 실행들과 비교한 변화 추이:
1. 2026-08-01 최초 실행: 1-3/4-1/4-2/4-4/4-5/4-6 실패 (OrderController 버그 발견 전), 2-1/3-3/7-2는 아직 수정 전이라 실패 예상 상태였음.
2. 2026-08-02 (2-1/3-3/7-2 수정 반영 후, `main` 브랜치): 1-3(401), 1-4(500, 버그 발견), 4-1/4-2/4-4/4-5/4-6(500, OrderController 버그 확인) 실패. 2-1/3-3/7-2는 통과로 전환됨.
3. 2026-08-02 (1-4 토큰+pre-request 스크립트 수정 후, 여전히 `main`): 1-4가 여전히 500이지만 원인이 토큰→UNIQUE 제약으로 바뀜. 나머지는 2번과 동일.
4. **2026-08-03 (`feature/api` 전환 후, 최신)**: 4-1/4-2/4-4/4-5 통과로 전환. 1-4는 500→409로 전환(원인 성격이 코드버그→테스트데이터로 바뀜). 4-6/5-4에서 새로운 500 발견.

---

## 6. 다음에 할 일 (제안, 우선순위 순)

1. **1-3 관리자 계정**: API 담당자가 Railway DB에 `admin01@seeat.com` 계정을 실제로 생성/시딩해줄 때까지 대기. 생성 후 재테스트해서 401이 사라지는지, OTP 관련 문제가 남아있는지 확인 필요.
2. **4-6, 5-4 신규 버그를 API 담당자에게 전달**: 이 문서와 함께 `Seeat_API_문제점_요약_API담당자용_0803.md`(같이 생성한 요약본) 전달.
3. **1-4 테스트 데이터 랜덤화**: 사용자가 원하면 Postman pre-request 스크립트에서 `businessRegistrationNumber`를 매 실행마다 랜덤 생성하도록 수정 (현재는 보류 상태).
4. **`Postman_test_0801-2.md` 최신화 여부**: 이 문서(AI용 상세본)와 별개로, 팀 공유용 문서(`Postman_test_0801-2.md`)에 4-6/5-4 신규 발견 사항과 OrderController 해결 확인을 반영할지 사용자에게 확인 후 진행.
5. **CRLF 노이즈 정리 (선택)**: `.gitattributes`에 `* text=auto` 추가를 API 담당자에게 제안할 수 있음 (급하지 않음, 코드 수정이라 QA가 직접 하지 않음).

---

## 7. 생성된 파일 목록 (전체, 2026-08-01~03 누적)

- `C:\Users\ChoSB\Desktop\Develop\Seeat\Postman_test_0801-2.md` — 팀원 공유용 요청/바디/상태 정리 문서 (2026-08-02까지 반영됨, 2026-08-03 신규 발견 사항은 아직 미반영 — 위 6-4 참고)
- `C:\Users\ChoSB\Desktop\Develop\Seeat\Seeat_API_테스트_상세정리_AI인수인계_0803.md` — 이 문서
- `C:\Users\ChoSB\Desktop\Develop\Seeat\Seeat_API_문제점_요약_API담당자용_0803.md` — API 담당자 전달용 간단 요약본
- `C:\Users\ChoSB\Desktop\Develop\Seeat\postman-test-assets\sample-product.png` — 2-1 테스트용 더미 이미지 파일 (68바이트, 1x1 PNG)
- `C:\Users\ChoSB\Desktop\Develop\seeat-api\.vscode\settings.json` — 로컬 실행 환경변수 설정 (JAVA_HOME, DB 접속정보 등)
- `인수인계_메모.md` (2026-08-01 작성, 사용자 업로드분) — 이 문서의 이전 버전. 브랜치 관련 내용(feature/api 전환)은 이 문서가 최신이므로 이 문서를 우선 참고할 것.
