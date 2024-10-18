-- 파일명: V6__add_index_on_product.sql
-- 파일 설명: product index 추가
-- 작성일: 2024-10-18
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

CREATE INDEX idx_product_id_name ON product (product_id, name);
