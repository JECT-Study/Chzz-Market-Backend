-- 파일명: V10__modify_field_name.sql
-- 파일 설명: 일부 테이블 필드 이름 변경
-- 작성일: 2024-11-5
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

ALTER TABLE `payment`
    CHANGE COLUMN `order_id` `order_no` VARCHAR(255) NOT NULL ;

-- 테이블 이름 변경
ALTER TABLE address RENAME TO delivery;

-- 컬럼 이름 변경
ALTER TABLE delivery CHANGE address_id delivery_id BIGINT NOT NULL AUTO_INCREMENT;
