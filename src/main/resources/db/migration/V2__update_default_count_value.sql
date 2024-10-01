-- 파일명: V2__update_default_count_value.sql
-- 파일 설명: `bid` 테이블의 `count` 컬럼의 기본값을 3에서 2로 변경
-- 작성일: 2024-10-02
-- 참고: Flyway 명명 규칙 "V<버전번호>__<설명>.sql"에 맞게 작성되었는지 확인해 주세요.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

ALTER TABLE `bid` ALTER COLUMN `count` SET DEFAULT 2;
