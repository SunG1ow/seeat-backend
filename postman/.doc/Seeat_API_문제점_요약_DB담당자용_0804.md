# Seeat API 테스트 중 발견된 문제 정리 (2026-08-04)

DB 스키마 확인 중 `order_id`를 참조하는 테이블 전체에 걸친 외래키 문제를 발견해서 별도로 정리합니다. 코드는 직접 수정하지 않았고, Railway DB(`seafood_dummy_test`)에서 직접 조회한 결과입니다.

---

## 주문(order) 관련 테이블이 두 벌 존재, 외래키가 옛날 테이블을 참조 중

### 현상

- 결제(4-2) 테스트에서 `Duplicate entry` 오류가 반복 발생 (order_id 3, 4가 이미 존재하는 더미 payment와 충돌)
- 주문 생성(4-1) 테스트에서 새로운 오류 발생: order_item insert 시 외래키 제약 조건 위반

```
Cannot add or update a child row: a foreign key constraint fails
(order_item, CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES `order` (order_id))
```

### 원인

DB에 주문 테이블이 **두 개** 존재합니다.

| 테이블 | 용도 | 현재 행 수 |
|---|---|---|
| `order` (단수) | 원래 손으로 설계된 테이블로 추정 (제약조건 이름이 `fk_xxx_xxx` 형태) | 30건 (더미데이터) |
| `orders` (복수) | 실제 엔티티가 매핑하는 테이블 | 4건 (테스트로 생성된 실 데이터) |

`Order.java` 엔티티 코드:

```java
@Entity
@Table(name = "orders")
public class Order { ... }
```

즉 코드는 `orders`(복수)를 쓰도록 매핑되어 있는데, DB에는 원래 `order`(단수)로 설계된 테이블이 있었던 것으로 보입니다. Hibernate가 엔티티 매핑에 맞춰 `orders` 테이블을 자동으로 새로 만든 것으로 추정됩니다(`orders` 테이블의 외래키 이름이 `FKd8ruqpq5q4ixexe61kxnxu...`처럼 자동 생성된 해시 이름인 반면, `order` 테이블과 나머지 테이블은 전부 사람이 지은 이름인 것이 근거입니다).

문제는 `order_id`를 참조하는 자식 테이블 6개가 전부 새 테이블이 아니라 옛날 `order`(단수)를 그대로 참조하고 있다는 점입니다. `information_schema.KEY_COLUMN_USAGE`로 확인한 결과:

| 테이블 | 제약조건 이름 | 참조 대상 (실제) | 참조해야 할 대상 |
|---|---|---|---|
| `delivery` | `fk_delivery_order` | `order` | `orders` |
| `notification` | `fk_notification_order` | `order` | `orders` |
| `order_item` | `fk_order_item_order` | `order` | `orders` |
| `order_status_history` | `fk_order_status_history_order` | `order` | `orders` |
| `payment` | `fk_payment_order` | `order` | `orders` |
| `settlement` | `fk_settlement_order` | `order` | `orders` |

지금까지는 `orders`(복수)에서 생성되는 주문 ID가 우연히 `order`(단수)의 더미데이터 ID 범위(1~30)와 겹쳤기 때문에 외래키 검사가 "우연히" 통과되어 왔습니다. 그 범위를 벗어나는 순간(혹은 애초에 겹치지 않는 ID일 때) 위와 같은 외래키 오류가 발생합니다. 결제 테스트에서 발생했던 `Duplicate entry` 문제도 같은 원인입니다 — 새로 생성된 주문 ID가 `order`(단수) 기준 더미 payment 데이터의 ID와 겹쳐서 발생한 충돌이었습니다.

### 영향 범위

order_item뿐 아니라 결제, 배송, 알림, 주문 상태 이력, 정산까지 주문과 연결되는 모든 기능이 같은 이유로 언제든 깨질 수 있는 상태입니다.

### 참고: 확인에 사용한 쿼리

```sql
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'seafood_dummy_test' AND REFERENCED_TABLE_NAME IS NOT NULL;
```

### 수정 제안 (검토 후 적용 부탁드립니다)

아래는 초안입니다. 실행 전에 팀에서 먼저 검토 부탁드립니다 — 특히 2단계는 `order`(단수)에만 연결된 더미데이터(26건 상당)를 삭제하는 작업이라 필요한 데이터인지 먼저 확인이 필요합니다.

**1단계. 영향받는 행 확인** (삭제 전 먼저 확인용)

```sql
SELECT 'delivery' AS tbl, order_id FROM delivery WHERE order_id NOT IN (SELECT order_id FROM orders)
UNION ALL
SELECT 'notification', order_id FROM notification WHERE order_id NOT IN (SELECT order_id FROM orders)
UNION ALL
SELECT 'order_item', order_id FROM order_item WHERE order_id NOT IN (SELECT order_id FROM orders)
UNION ALL
SELECT 'order_status_history', order_id FROM order_status_history WHERE order_id NOT IN (SELECT order_id FROM orders)
UNION ALL
SELECT 'payment', order_id FROM payment WHERE order_id NOT IN (SELECT order_id FROM orders)
UNION ALL
SELECT 'settlement', order_id FROM settlement WHERE order_id NOT IN (SELECT order_id FROM orders);
```

**2단계. `order`(단수)에만 연결된 더미데이터 정리** (1단계 결과 확인 후, 불필요한 더미데이터라고 판단되면 진행)

```sql
DELETE FROM settlement WHERE order_id NOT IN (SELECT order_id FROM orders);
DELETE FROM payment WHERE order_id NOT IN (SELECT order_id FROM orders);
DELETE FROM order_status_history WHERE order_id NOT IN (SELECT order_id FROM orders);
DELETE FROM order_item WHERE order_id NOT IN (SELECT order_id FROM orders);
DELETE FROM notification WHERE order_id NOT IN (SELECT order_id FROM orders);
DELETE FROM delivery WHERE order_id NOT IN (SELECT order_id FROM orders);
```

**3단계. 외래키를 `orders`(복수)로 재연결**

```sql
ALTER TABLE delivery DROP FOREIGN KEY fk_delivery_order;
ALTER TABLE delivery ADD CONSTRAINT fk_delivery_order FOREIGN KEY (order_id) REFERENCES orders(order_id);

ALTER TABLE notification DROP FOREIGN KEY fk_notification_order;
ALTER TABLE notification ADD CONSTRAINT fk_notification_order FOREIGN KEY (order_id) REFERENCES orders(order_id);

ALTER TABLE order_item DROP FOREIGN KEY fk_order_item_order;
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(order_id);

ALTER TABLE order_status_history DROP FOREIGN KEY fk_order_status_history_order;
ALTER TABLE order_status_history ADD CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(order_id);

ALTER TABLE payment DROP FOREIGN KEY fk_payment_order;
ALTER TABLE payment ADD CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(order_id);

ALTER TABLE settlement DROP FOREIGN KEY fk_settlement_order;
ALTER TABLE settlement ADD CONSTRAINT fk_settlement_order FOREIGN KEY (order_id) REFERENCES orders(order_id);
```

**4단계 (선택, 3단계까지 완료 후 참조하는 곳이 없어지면).** `order`(단수) 테이블을 이후에도 쓸 계획이 없다면 정리:

```sql
-- DROP TABLE `order`;
```

---

## 요약

| 항목 | 상태 | 필요 조치 |
|---|---|---|
| `order`/`orders` 테이블 이원화 + 외래키 6개 오참조 | 미해결 | 위 SQL 검토 후 적용, 또는 다른 방식으로 스키마 정리 필요 |
