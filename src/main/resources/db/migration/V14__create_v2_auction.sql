-- 파일명: V14__create_v2_auction.sql
-- 파일 설명: v2 auction 테이블 생성
-- 작성일: 2024-11-8
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

CREATE TABLE auction_v2
(
    auction_id    BIGINT AUTO_INCREMENT NOT NULL,
    seller_id     BIGINT                NULL,
    name          VARCHAR(255)          NOT NULL,
    `description` VARCHAR(1000)         NULL,
    min_price     INT                   NULL,
    category      VARCHAR(30)           NOT NULL,
    status        VARCHAR(20)           NULL,
    end_date_time datetime              NULL,
    winner_id     BIGINT                NULL,
    like_count    INT                   NULL,
    bid_count     INT                   NULL,
    created_at    datetime              NULL,
    updated_at    datetime              NULL,
    CONSTRAINT pk_auction_v2 PRIMARY KEY (auction_id)
);

ALTER TABLE auction_v2
    ADD CONSTRAINT FK_AUCTION_V2_ON_SELLER FOREIGN KEY (seller_id) REFERENCES users (user_id);
