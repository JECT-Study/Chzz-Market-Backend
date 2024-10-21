-- 파일명: V6__add_address_columns.sql
-- 파일 설명: address 테이블에 배송지 주소 관련 컬럼 추가
-- 작성일: 2024-10-20
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

-- 새로운 컬럼 추가
ALTER TABLE `address`
    ADD COLUMN `recipient_name` VARCHAR(255) NOT NULL AFTER `detail_address`,
    ADD COLUMN `phone_number`   VARCHAR(20)  NOT NULL AFTER `recipient_name`,
    ADD COLUMN `is_default`     BIT(1)       NOT NULL DEFAULT 0 AFTER `phone_number`;

-- 기존 필드에 nullable = false 추가
ALTER TABLE `address`
    MODIFY COLUMN `road_address` VARCHAR(255) NOT NULL,
    MODIFY COLUMN `jibun` VARCHAR(255) NOT NULL,
    MODIFY COLUMN `zipcode` VARCHAR(20) NOT NULL,
    MODIFY COLUMN `detail_address` VARCHAR(255) NOT NULL;