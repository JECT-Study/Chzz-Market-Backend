-- 파일명: V7__add_orders_table.sql
-- 파일 설명: 주문 테이블 추가
-- 작성일: 2024-10-21
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

-- order 테이블 추가
CREATE TABLE orders (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    auction_id BIGINT,
    buyer_id BIGINT NOT NULL,
    payment_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    order_no VARCHAR(255) NOT NULL,
    method VARCHAR(30) NOT NULL,
    zipcode VARCHAR(255) NOT NULL,
    road_address VARCHAR(255) NOT NULL,
    jibun VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    delivery_memo VARCHAR(255),
    PRIMARY KEY (order_id)
) ENGINE=InnoDB;


-- auction 테이블과 연관관계 설정
ALTER TABLE orders
    ADD CONSTRAINT FKjn4msbk22y92rmkpf4qa097sv
    FOREIGN KEY (auction_id) REFERENCES auction (auction_id);
