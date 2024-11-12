-- 파일명: V15__create_like_image.sql
-- 파일 설명: v2 like, image
-- 작성일: 2024-11-12
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

CREATE TABLE imagev2
(
    image_id   BIGINT AUTO_INCREMENT NOT NULL,
    auction_id BIGINT                NULL,
    cdn_path   VARCHAR(255)          NOT NULL,
    sequence   INT                   NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    CONSTRAINT pk_imagev2 PRIMARY KEY (image_id)
);

CREATE TABLE likes
(
    like_id    BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT                NOT NULL,
    auction_id BIGINT                NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    CONSTRAINT pk_likes PRIMARY KEY (like_id),
    CONSTRAINT uc_likes_user_auction UNIQUE (user_id, auction_id)
);

CREATE INDEX idx_15d5b1726f3551b467ed898fd ON imagev2 (auction_id, image_id, cdn_path);

ALTER TABLE imagev2
    ADD CONSTRAINT FK_IMAGEV2_ON_AUCTION FOREIGN KEY (auction_id) REFERENCES auction_v2 (auction_id);
