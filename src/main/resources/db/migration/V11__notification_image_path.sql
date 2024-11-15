-- 파일명: V11__notification_image_path.sql
-- 파일 설명: notification 테이블에 cdn_path 컬럼을 추가하고 image_id 컬럼을 제거합니다.
-- 작성일: 2024-11-5
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

-- 1. cdn_path 컬럼 추가
ALTER TABLE notification
    ADD COLUMN cdn_path VARCHAR(255) DEFAULT NULL AFTER is_deleted;

-- 2. cdn_path 데이터 업데이트
UPDATE notification n
    JOIN image i ON n.image_id = i.image_id
SET n.cdn_path = i.cdn_path;

-- 3. image_id 컬럼 및 외래 키 제약 조건 삭제
ALTER TABLE notification
    DROP FOREIGN KEY FKholipoc9p58ukvigqmd8ejvoo;

ALTER TABLE notification
    DROP COLUMN image_id;
