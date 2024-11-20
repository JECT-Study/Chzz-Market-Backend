-- 파일명: V16__create_v2_order_payment.sql
-- 파일 설명: v2 order, payment, 경매 API 전환완료시 삭제 예정
-- 작성일: 2024-11-18
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

CREATE TABLE orders_v2
(
    order_id       BIGINT AUTO_INCREMENT NOT NULL,
    order_no       VARCHAR(255)          NOT NULL,
    buyer_id       BIGINT                NOT NULL,
    payment_id     BIGINT                NOT NULL,
    amount         BIGINT                NOT NULL,
    delivery_memo  VARCHAR(255)          NULL,
    road_address   VARCHAR(255)          NOT NULL,
    jibun          VARCHAR(255)          NOT NULL,
    zipcode        VARCHAR(255)          NOT NULL,
    detail_address VARCHAR(255)          NOT NULL,
    recipient_name VARCHAR(255)          NOT NULL,
    phone_number   VARCHAR(255)          NOT NULL,
    method         VARCHAR(30)           NOT NULL,
    auction_id     BIGINT                NULL,
    CONSTRAINT pk_orders_v2 PRIMARY KEY (order_id)
);

CREATE TABLE paymentv2
(
    payment_id  BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime              NULL,
    updated_at  datetime              NULL,
    user_id     BIGINT                NOT NULL,
    auction_id  BIGINT                NOT NULL,
    amount      BIGINT                NOT NULL,
    method      VARCHAR(30)           NOT NULL,
    status      VARCHAR(30)           NOT NULL,
    order_no    VARCHAR(255)          NOT NULL,
    payment_key VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_paymentv2 PRIMARY KEY (payment_id)
);

ALTER TABLE paymentv2
    ADD CONSTRAINT uc_paymentv2_orderno UNIQUE (order_no);

ALTER TABLE orders_v2
    ADD CONSTRAINT FK_ORDERS_V2_ON_AUCTION FOREIGN KEY (auction_id) REFERENCES auction_v2 (auction_id);

ALTER TABLE paymentv2
    ADD CONSTRAINT FK_PAYMENTV2_ON_AUCTION FOREIGN KEY (auction_id) REFERENCES auction_v2 (auction_id);

ALTER TABLE paymentv2
    ADD CONSTRAINT FK_PAYMENTV2_ON_USER FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE auction_v2
    MODIFY COLUMN bid_count BIGINT NULL,
    MODIFY COLUMN like_count BIGINT NULL;
