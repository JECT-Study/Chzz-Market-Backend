-- 파일명: V12__modify_bid_field.sql
-- 파일 설명: bid 테이블의 필드 수정
-- 작성일: 2024-11-5
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

-- 외래키 제약 조건 삭제
ALTER TABLE `bid`
    DROP FOREIGN KEY `FKhexc6i4j8i0tmpt8bdulp6g3g`,
    DROP FOREIGN KEY `FKi1pwg1muxilapowsmifod8jtf`;

-- user_id 컬럼 이름을 bidder_id로 변경
ALTER TABLE `bid`
    CHANGE COLUMN `user_id` `bidder_id` bigint NOT NULL;

-- 인덱스 이름을 auction_id_idx와 bidder_id_idx로 변경
ALTER TABLE `bid`
    DROP INDEX `FKhexc6i4j8i0tmpt8bdulp6g3g`,
    DROP INDEX `FKi1pwg1muxilapowsmifod8jtf`,
    ADD INDEX `auction_id_idx` (`auction_id`),
    ADD INDEX `bidder_id_idx` (`bidder_id`);
