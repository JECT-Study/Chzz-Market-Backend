-- 파일명: V4__add_profile_image_url_to_users.sql
-- 파일 설명: users 테이블에 프로필 이미지 URL(profile_image_url) 컬럼 추가
-- 작성일: 2024-10-05
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

ALTER TABLE `users`
    ADD COLUMN `profile_image_url` varchar(255) DEFAULT NULL AFTER `link`;
