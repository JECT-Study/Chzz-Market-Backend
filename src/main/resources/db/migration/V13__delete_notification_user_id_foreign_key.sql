-- 파일명: V13__delete_notification_user_id_foreign_key.sql
-- 파일 설명: notification 테이블의 외래키 제거 및 인덱스 이름 변경
-- 작성일: 2024-11-5
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

-- user_id 외래키 제약 조건 삭제
ALTER TABLE notification
    DROP FOREIGN KEY FKnk4ftb5am9ubmkv1661h15ds9;

-- user_id 인덱스 이름을 user_id_idx로 변경
ALTER TABLE notification
    RENAME INDEX FKnk4ftb5am9ubmkv1661h15ds9 TO user_id_idx;
