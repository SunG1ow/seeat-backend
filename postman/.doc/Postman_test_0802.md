# Seeat API Postman 테스트 케이스 정리

- Workspace: `Seeat Project - Dummy Data`
- Collection: `SEEAT API v1.1.4 Copy`
- Environment: `Seeat Local - Dummy Data`
- 기준 서버: 로컬 (`{{baseUrl}} = http://localhost:8081`)
- 정리 시점: 2026-08-01 작성, 2026-08-02 재테스트 결과 반영 (Postman CLI `postman collection run` 실행 기준)

## 환경 변수 (Environment)

| key | value | 설명 |
|---|---|---|
| baseUrl | http://localhost:8081 | 로컬 서버 주소 |
| buyerAccessToken | (자동 세팅, secret) | 1-2a 실행 후 저장되는 구매자 JWT |
| sellerAccessToken | (자동 세팅, secret) | 1-2b 실행 후 저장되는 판매자 JWT |
| adminAccessToken | (자동 세팅, secret) | 1-3 실행 후 저장되는 관리자 JWT |
| tempAccessToken | (자동 세팅, secret) | 1-1b 실행 후 저장되는 임시(탈퇴 테스트용) 계정 JWT |
| orderId | 3 (기본값) | 4-1 실행 후 자동으로 실제 생성된 주문ID로 덮어씀 |
| cartProductId | 1 (기본값) | 3-2 실행 후 자동으로 덮어씀 |
| addressId | 1 (기본값) | 5-2b 실행 시 자동으로 덮어씀. 더미데이터 buyer08 소유 주소ID |
| productId | 1 (기본값) | "제주 활광어" (ON_SALE, seller01 소유) 실제 더미 상품. 2-1 성공 시 새로 생성된 상품ID로 덮어씀 |
| categoryId | 5 | "광어" leaf 카테고리 (2-1 상품등록 필요값) |
| userId | (자동 세팅) | 1-1 회원가입 응답의 userId |
| buyerEmail / sellerEmail | (자동 세팅) | 1-2a/1-2b pre-request에서 매 실행마다 새로 생성되는 테스트 계정 이메일 |
| tempBuyerEmail | (자동 세팅) | 1-1 pre-request에서 생성되는 임시(탈퇴용) 계정 이메일 |
| testPassword | testPass1234! | 자체 생성 buyer/seller 테스트 계정 공통 비밀번호 |
| adminEmail | admin01@seeat.com | API 담당자 제공 실제 관리자 계정 |
| adminPassword | adminPassword1! (secret) | API 담당자 제공 실제 관리자 비밀번호 |

## 인증 토큰 구조

이전에는 `accessToken` 변수 하나를 buyer/seller/admin이 공유해서 Runner 한 번에 여러 요청이 서로 토큰을 덮어쓰는 문제가 있었음. 현재는 역할별로 분리:
`buyerAccessToken`, `sellerAccessToken`, `adminAccessToken`, `tempAccessToken` (탈퇴 테스트 전용, 1-1→1-1b→5-4로 연결되어 더미 계정을 건드리지 않음).

buyer/seller 로그인은 더미 계정(buyer08 등, 비밀번호 불명) 대신 **매 실행마다 자체 회원가입 후 로그인**하는 방식으로 변경됨 (pre-request script에서 `pm.sendRequest()`로 `/api/v1/auth/signup` 호출 → 방금 만든 계정으로 로그인).

---

## 1. 인증 / 회원

### 1-1. 구매자/판매자 회원가입
- `POST {{baseUrl}}/api/v1/auth/signup`, No Auth
- Pre-request: `tempBuyerEmail = temp_buyer_{timestamp}@example.com` 생성
- Body:
```json
{
  "email": "{{tempBuyerEmail}}",
  "password": "password1234",
  "role": "BUYER",
  "nickname": "임시테스트계정",
  "phoneNumber": "010-1234-5678"
}
```
- Test: 201, `data.userId`/`data.email`/`data.role=="BUYER"` 존재 확인, `userId` 저장
- 상태: **정상 통과**

### 1-2a. 구매자 로그인 (자체 생성 테스트 계정)
- `POST {{baseUrl}}/api/v1/auth/login`, No Auth
- Pre-request: `test_buyer_{timestamp}@example.com`로 `/api/v1/auth/signup` (role=BUYER, password={{testPassword}}) 먼저 호출 → `buyerEmail`에 저장
- Body: `{"email": "{{buyerEmail}}", "password": "{{testPassword}}"}`
- Test: 200, `accessToken`/`refreshToken` → `buyerAccessToken`/`buyerRefreshToken` 저장
- 상태: **정상 통과**

### 1-3. 관리자 계정 로그인
- `POST {{baseUrl}}/api/v1/admin/login`, No Auth
- Body: `{"email": "{{adminEmail}}", "password": "{{adminPassword}}", "otpCode": "123456"}`
- Test: 200, `data.accessToken`, `data.role=="ADMIN"` → `adminAccessToken` 저장
- **상태: 401 실패.** 서버 코드상 `admin01@seeat.com` 계정이 로컬이 붙는 DB에 없거나(INVALID_CREDENTIALS), 있어도 OTP 시크릿키가 없어서(INVALID_OTP) 발생 — 둘 다 401이라 응답만으로는 구분 불가. **API 담당자 확인 필요.**

### 1-4. 판매자 사업자 인증
- `POST {{baseUrl}}/api/v1/auth/verify-business`, Bearer `{{sellerAccessToken}}` (2026-08-02: 원래 `{{buyerAccessToken}}`로 잘못 걸려있던 것을 수정. 사업자 인증은 SELLER 권한이 필요한데 BUYER 토큰으로 호출하고 있었음)
- Pre-request (2026-08-02 추가): `sellerAccessToken`이 비어있으면 1-2b와 동일한 방식으로 판매자 계정을 자체 가입+로그인해서 채워넣음 → 폴더 실행 순서와 무관하게 항상 유효한 SELLER 토큰으로 호출됨
- Body:
```json
{
  "businessRegistrationNumber": "1234567890",
  "representativeName": "홍길동",
  "openingDate": "20200101"
}
```
- Test: 200, `data.verified===true`, `data.authStatus=="VERIFIED"`
- **상태: 500 실패 (2026-08-02 재테스트, 토큰 수정 후에도 재현됨).** 원인이 토큰 문제가 아니라 별도의 백엔드 버그로 확인됨: `SellerBusinessInfo` 엔티티(`SellerBusinessInfo.java` 21행)의 `business_registration_number` 컬럼이 `unique = true`인데, 이 Body의 `businessRegistrationNumber` 값이 `"1234567890"` 고정값. 최초 1회 성공 이후로는 실행할 때마다 새 판매자 계정이 동일한 고정값으로 인증을 시도하게 되어 DB UNIQUE 제약 위반이 발생함. `AuthService.verifyBusiness()`(96~127행)에는 중복 여부를 사전 검사하는 로직이 없어서 DB 예외가 그대로 터지고, `GlobalExceptionHandler`의 catch-all(`Exception.class` → 42~47행)이 이를 500으로 뭉뚱그림. **API 담당자 확인/수정 필요**: (1) 중복 사업자등록번호에 대한 명시적 검증/에러코드 추가, (2) Postman 쪽에서도 테스트값을 매번 다르게 생성하도록 바꿔야 재현 가능한 테스트가 됨(테스트 데이터 이슈, 코드 수정과 별개).

### 1-2b. 판매자 로그인 (자체 생성 테스트 계정)
- `POST {{baseUrl}}/api/v1/auth/login`, No Auth
- Pre-request: `test_seller_{timestamp}@example.com`로 `/api/v1/auth/signup` (role=SELLER, password={{testPassword}}) 먼저 호출 → `sellerEmail`에 저장
- Body: `{"email": "{{sellerEmail}}", "password": "{{testPassword}}"}`
- Test: 200, `accessToken` → `sellerAccessToken`/`sellerRefreshToken` 저장
- 참고: 가입 시 role=SELLER로 바로 생성되므로 1-4(사업자 인증) 없이도 SELLER 권한 토큰 발급됨
- 상태: **정상 통과**

### 1-1b. 임시계정 로그인 (탈퇴 테스트용)
- `POST {{baseUrl}}/api/v1/auth/login`, No Auth
- Body: `{"email": "{{tempBuyerEmail}}", "password": "password1234"}`
- Test: 200, `accessToken` → `tempAccessToken` 저장
- 상태: **정상 통과**

---

## 2. 상품

### 2-4a. 상품 문의 등록
- `POST {{baseUrl}}/api/v1/products/{{productId}}/faqs`, Bearer `{{buyerAccessToken}}`
- Body: `{"content": "냉동 상태로 오나요?"}`
- Test: 2xx
- 상태: **정상 통과**

### 2-4b. 상품 문의 조회
- `GET {{baseUrl}}/api/v1/products/{{productId}}/faqs`, Bearer `{{buyerAccessToken}}`
- Test: 200, `res.content`가 배열, 있으면 `faqId` 속성 확인
- 상태: **정상 통과**

### 2-1. 수산물 상품 등록
- `POST {{baseUrl}}/api/v1/products`, Bearer `{{sellerAccessToken}}`, multipart/form-data
- Body (2026-08-02 채움):

| key | type | 예시값 |
|---|---|---|
| categoryId | text | {{categoryId}} |
| name | text | 테스트 활광어 |
| origin | text | 제주 |
| storageType | text | 냉장 |
| weight | text | 1.5 |
| weightUnit | text | kg |
| isMandatoryAuction | text | false |
| price | text | 15000 |
| stockQuantity | text | 50 |
| images | file | `postman-test-assets/sample-product.png` (테스트용 1x1 PNG) |

- Test: 201, `data.productId` 존재, `data.status=="PENDING_REVIEW"` → `productId` 저장
- **상태: 정상 통과 (2026-08-02 재테스트 완료).** 원래 바디가 비어있어서(필수 파라미터 `images` 누락) 500이었으나, 위 표대로 채운 뒤 재실행하여 201 확인함.

### 2-2. 수산물 분류 조회
- `GET {{baseUrl}}/api/v1/products/categories`, No Auth
- Test: 200, `data`가 배열, `children` 속성 확인
- 상태: **정상 통과**

### 2-3. 필터 및 정렬 검색
- `GET {{baseUrl}}/api/v1/products/search?category=&origin=&priceMin=&priceMax=&storageType=&sort=LATEST&page=0&size=20`, No Auth
- Test: 200, `content`가 배열, `page.totalElements` 확인
- 상태: **정상 통과**

---

## 3. 장바구니

### 3-1. 장바구니 조회
- `GET {{baseUrl}}/api/v1/cart`, Bearer `{{buyerAccessToken}}`
- Test: 200, `data.cartId`, `data.items` 배열 확인
- 상태: **정상 통과**

### 3-2. 장바구니 상품 추가
- `POST {{baseUrl}}/api/v1/cart/items`, Bearer `{{buyerAccessToken}}`
- Body: `{"productId": {{productId}}, "quantity": 2}`
- Test: 201, `data.cartProductId`, `data.productId` 일치 확인 → `cartProductId` 저장
- 상태: **정상 통과**

### 3-3. 장바구니 상품 삭제
- `DELETE {{baseUrl}}/api/v1/cart/items/{{cartProductId}}`, Bearer `{{buyerAccessToken}}`
- Test: 200, `res.success===true`
- 상태: **정상 통과** (원래 `res.data`를 `null`과 비교해서 실패했었음 — 서버가 `@JsonInclude(NON_ABSENT)`라 null 필드를 아예 응답에서 생략함. `success` 값만 확인하도록 수정)

---

## 4. 주문 / 결제 / 배송

### 4-1. 주문서 작성
- `POST {{baseUrl}}/api/v1/orders`, Bearer `{{buyerAccessToken}}`
- Body:
```json
{
  "items": [{ "productId": {{productId}}, "quantity": 2 }],
  "addressId": {{addressId}},
  "requestMessage": "부재 시 경비실에 맡겨주세요"
}
```
- Test: 201, `data.orderStatus=="PAYMENT_PENDING"`, `data.orderId`/`data.totalAmount` 존재 → `orderId` 저장
- **상태: 500 실패.** 백엔드 `OrderController` 클래스에 `@RequestMapping("/api/orders")`가 붙어 있는데 메서드마다 또 절대경로(`/api/v1/orders`)를 써서 실제 등록 경로가 `/api/orders/api/v1/orders`로 꼬여 있음. Postman이 부르는 `/api/v1/orders`와 매칭 안 됨. **API 담당자 백엔드 수정 필요** (Postman 쪽에서 고칠 수 있는 문제 아님).

### 4-2. 결제 시도 / 승인
- `POST {{baseUrl}}/api/v1/orders/{{orderId}}/payment`, Bearer `{{buyerAccessToken}}`
- Body: `{"paymentMethod": "CARD", "pgTransactionId": "tx_{{$timestamp}}"}`
- Test: 200, `data.orderStatus=="PAYMENT_COMPLETED"`, `data.paymentId` 존재
- **상태: 500 실패.** 4-1과 동일한 라우팅 버그 + 4-1이 실패해서 `orderId`가 갱신되지 않는 연쇄 문제.

### 4-3. 주문 상태 실시간 변경 (관리자)
- `PATCH {{baseUrl}}/api/v1/orders/{{orderId}}/status`, Bearer `{{adminAccessToken}}`
- Body: `{"status": "SHIPPING", "carrier": "CJ대한통운", "trackingNumber": "123456789012"}`
- Test: 200, `data.status=="SHIPPING"`, `data.notifiedAt` 존재
- **상태: 401 실패.** 이건 라우팅 버그와 무관 — `AdminOrderController`가 이 경로를 정상적으로 처리 중이라 401(관리자 로그인 실패로 토큰 없음)이 그대로 뜸. 1-3만 해결되면 정상 동작 예상.

### 4-4. 주문 상태 이력 조회
- `GET {{baseUrl}}/api/v1/orders/{{orderId}}/status-history`, Bearer `{{buyerAccessToken}}`
- Test: 200, `data`가 배열, 있으면 `statusValue` 속성 확인
- **상태: 500 실패.** 4-1과 동일한 라우팅 버그.

### 4-5. 사용자 구매 내역 조회
- `GET {{baseUrl}}/api/v1/users/me/orders?page=0&size=20`, Bearer `{{buyerAccessToken}}`
- Test: 200, `content`가 배열, `page.totalPages` 확인
- **상태: 500 실패.** 동일한 라우팅 버그.

### 4-6. 주문 내역 및 배송 추적
- `GET {{baseUrl}}/api/v1/users/me/delivery?startDate=2026-07-01&endDate=2026-07-24&page=0&size=20`, Bearer `{{buyerAccessToken}}`
- Test: 200, `content`가 배열, 있으면 `trackingNumber` 속성 확인
- **상태: 500 실패.** 동일한 라우팅 버그.

> **참고 (백엔드 버그 상세, API 담당자 전달용):**
> `OrderController.java`가 `@RequestMapping("/api/orders")` (클래스 레벨) + 각 메서드마다 `/api/v1/orders...` (절대경로)를 같이 쓰고 있어서, Spring이 이를 이어붙여 실제 등록 경로가 `/api/orders/api/v1/orders`처럼 됩니다. 클래스 레벨 `@RequestMapping`을 지우면 되는데, 그러면 `PATCH /api/v1/orders/{orderId}/status`가 `AdminOrderController`와 완전히 겹쳐서 서버 기동 자체가 실패(Ambiguous mapping)하니, `OrderController` 안의 중복된 `changeStatus` 메서드(4-3 관련, 이미 `AdminOrderController`가 처리 중)도 같이 제거해야 합니다.

---

## 5. 사용자

### 5-1a. 프로필 조회
- `GET {{baseUrl}}/api/v1/users/me`, Bearer `{{buyerAccessToken}}`
- Test: 200, `userId`/`email`/`nickname` 존재
- 상태: **정상 통과**

### 5-1b. 프로필 수정
- `PUT {{baseUrl}}/api/v1/users/me`, Bearer `{{buyerAccessToken}}`
- Test: 200, `data.nickname` 존재
- 상태: **정상 통과**

### 5-2a. 배송지 목록 조회
- `GET {{baseUrl}}/api/v1/users/me/addresses`, Bearer `{{buyerAccessToken}}`
- Test: 200, `data`(또는 `content`)가 배열
- 상태: **정상 통과**

### 5-2b. 배송지 추가
- `POST {{baseUrl}}/api/v1/users/me/addresses`, Bearer `{{buyerAccessToken}}`
- Body: `{"alias": "집", "receiverName": "홍길동", "receiverPhone": "010-1234-5678", "address": "서울시 강남구 테헤란로 1", "isDefault": true}`
- Test: 2xx, 있으면 `addressId` → 환경변수 저장
- 상태: **정상 통과**

### 5-2c. 배송지 삭제
- `DELETE {{baseUrl}}/api/v1/users/me/addresses/{{addressId}}`, Bearer `{{buyerAccessToken}}`
- Test: 200
- 상태: **정상 통과**

### 5-3. 알림 목록 조회
- `GET {{baseUrl}}/api/v1/users/me/notifications?type=&isRead=&page=0&size=20`, Bearer `{{buyerAccessToken}}`
- Test: 200, `content`가 배열, 있으면 `isRead` 속성 확인
- 상태: **정상 통과**

### 5-4. 회원 탈퇴
- `DELETE {{baseUrl}}/api/v1/users/me`, Bearer `{{tempAccessToken}}` (더미 계정 보호를 위해 1-1/1-1b에서 만든 임시 계정만 사용)
- Body: `{"password": "password1234", "reason": "서비스 이용 안함"}`
- Test: 200, `res.success===true`
- 상태: **정상 통과**

---

## 6. 신고

### 6-1. 신고 등록
- `POST {{baseUrl}}/api/v1/reports`, Bearer `{{buyerAccessToken}}`
- Body: `{"targetType": "PRODUCT", "targetId": {{productId}}, "reason": "원산지 표시 의심"}`
- Test: 201, `data.status=="PENDING"`, `data.reportId` 존재
- 상태: **정상 통과**

---

## 7. 판매자 전용

### 7-1. 상품 및 매출관리 대시보드
- `GET {{baseUrl}}/api/v1/seller/dashboard?startDate=&endDate=&page=0&size=20`, Bearer `{{sellerAccessToken}}`
- Test: 200, `data.products.content`, `data.salesSummary.totalSalesAmount` 존재
- 상태: **정상 통과**

### 7-2. 정산 내역 조회
- `GET {{baseUrl}}/api/v1/seller/settlements?status=&page=0&size=20`, Bearer `{{sellerAccessToken}}`
- Test: 200, `data`가 배열, 있으면 `settlementId` 속성 확인
- 상태: **정상 통과** (원래 페이지네이션 형식(`res.content`)으로 검증했는데, 실제 응답은 `ApiResponse<List<...>>` 구조라 `res.data`가 맞음 — 수정함)

---

## 정리: 백엔드(API 담당자) 확인/수정 필요 항목

**2026-08-02 재테스트 기준, 34개 요청 중 test-scripts 30 통과 / 2 실패, assertions 57 통과 / 16 실패. 아래 미해결 항목들이 원인.**

1. **1-3 관리자 로그인 401 (미해결)** — `admin01@seeat.com` 계정이 로컬 연결 DB(Railway `seafood_dummy_test`)에 실제로 있는지, OTP 시크릿키가 세팅되어 있는지 확인 필요. Body의 `otpCode`가 현재 `"123456"` 고정값인데, 실제 TOTP라면 30초마다 값이 바뀌므로 이 고정값 자체가 항상 틀릴 가능성이 높음 — OTP 검증 방식(고정 테스트 코드 허용 여부 등)을 API 담당자에게 확인 필요.
2. **OrderController 라우팅 버그 (미해결)** — 4-1, 4-2, 4-4, 4-5, 4-6 전부 이 하나의 버그 때문에 500. 클래스 레벨 `@RequestMapping("/api/orders")` 제거 + `AdminOrderController`와 겹치는 중복 `changeStatus` 메서드 정리 필요 (자세한 내용은 4번 섹션 하단 참고). 4-3은 같은 버그가 아니라 1-3 실패의 연쇄로 401.
3. **1-4 사업자 인증 UNIQUE 제약 위반 (미해결, 신규 발견)** — `SellerBusinessInfo.businessRegistrationNumber` 컬럼이 `unique=true`인데 서비스 로직(`AuthService.verifyBusiness()`)에 중복 검증이 없어서, 이미 사용된 사업자등록번호로 재인증 시도 시 DB 예외가 그대로 500으로 노출됨. 정상적인 케이스라면 409/400 등으로 명확히 응답해야 할 상황. API 담당자 확인 필요.
4. **공통 문제: 예외 처리가 전부 500으로 뭉뚱그려짐** — `GlobalExceptionHandler`(`GlobalExceptionHandler.java` 42~47행)에 `@ExceptionHandler(Exception.class)` catch-all이 있어서, 라우팅 미스매치(2번 항목, 원래는 404여야 함)나 DB 제약 위반(3번 항목, 원래는 409/400이어야 함) 같은 서로 다른 원인들이 전부 500 하나로만 응답됨. 클라이언트/QA 입장에서 응답 코드만으로 원인을 구분할 수 없어 디버깅이 어려움 — 예외 유형별 세분화된 처리를 권장.
5. **2-1 상품 등록 (해결됨)** — 코드 버그는 아니고 Postman 요청 바디가 비어있어서 발생. 위 표대로 채워서 2026-08-02 재테스트 통과 확인.
