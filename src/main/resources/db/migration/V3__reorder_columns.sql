-- 파일명: V3__reorder_columns.sql
-- 파일 설명: 가독성을 위해 컬럼 순서 변경
-- 작성일: 2024-10-02
-- 참고: 이 파일은 Flyway 명명 규칙 "V<버전번호>__<설명>.sql"을 따릅니다.
--      적용된 후에는 절대 수정할 수 없으므로, 수정이 필요한 경우에는 새로운 마이그레이션 파일을 작성해 주세요.

ALTER TABLE `users`
    CHANGE COLUMN `email` `email` VARCHAR(255) NOT NULL AFTER `nickname`,
    CHANGE COLUMN `customer_key` `customer_key` BINARY(16) NOT NULL AFTER `provider_type`,
    CHANGE COLUMN `created_at` `created_at` DATETIME(6) NULL DEFAULT NULL AFTER `user_role`,
    CHANGE COLUMN `updated_at` `updated_at` DATETIME(6) NULL DEFAULT NULL AFTER `created_at`;

ALTER TABLE `product`
    CHANGE COLUMN `name` `name` VARCHAR(255) NOT NULL AFTER `user_id`,
    CHANGE COLUMN `min_price` `min_price` INT NULL DEFAULT NULL AFTER `category`,
    CHANGE COLUMN `created_at` `created_at` DATETIME(6) NULL DEFAULT NULL AFTER `min_price`,
    CHANGE COLUMN `updated_at` `updated_at` DATETIME(6) NULL DEFAULT NULL AFTER `created_at`;

ALTER TABLE `auction`
    CHANGE COLUMN `product_id` `product_id` BIGINT NULL DEFAULT NULL AFTER `auction_id`,
    CHANGE COLUMN `status` `status` VARCHAR(20) NULL DEFAULT NULL AFTER `product_id`,
    CHANGE COLUMN `end_date_time` `end_date_time` DATETIME(6) NULL DEFAULT NULL AFTER `status`,
    CHANGE COLUMN `winner_id` `winner_id` BIGINT NULL DEFAULT NULL AFTER `end_date_time`;

ALTER TABLE `image`
    CHANGE COLUMN `cdn_path` `cdn_path` VARCHAR(255) NOT NULL AFTER `product_id`,
    CHANGE COLUMN `created_at` `created_at` DATETIME(6) NULL DEFAULT NULL AFTER `cdn_path`;

ALTER TABLE `notification`
    CHANGE COLUMN `notification_id` `notification_id` BIGINT NOT NULL AUTO_INCREMENT FIRST,
    CHANGE COLUMN `user_id` `user_id` BIGINT NOT NULL AFTER `notification_id`,
    CHANGE COLUMN `image_id` `image_id` BIGINT NULL DEFAULT NULL AFTER `user_id`,
    CHANGE COLUMN `auction_id` `auction_id` BIGINT NULL DEFAULT NULL AFTER `image_id`,
    CHANGE COLUMN `type` `type` VARCHAR(31) NOT NULL AFTER `auction_id`,
    CHANGE COLUMN `message` `message` VARCHAR(255) NOT NULL AFTER `type`,
    CHANGE COLUMN `is_read` `is_read` BIT(1) NOT NULL AFTER `message`;

ALTER TABLE `address`
    CHANGE COLUMN `user_id` `user_id` BIGINT NOT NULL AFTER `address_id`,
    CHANGE COLUMN `zipcode` `zipcode` VARCHAR(255) NULL DEFAULT NULL AFTER `user_id`,
    CHANGE COLUMN `road_address` `road_address` VARCHAR(255) NULL DEFAULT NULL AFTER `zipcode`,
    CHANGE COLUMN `jibun` `jibun` VARCHAR(255) NULL DEFAULT NULL AFTER `road_address`,
    CHANGE COLUMN `detail_address` `detail_address` VARCHAR(255) NULL DEFAULT NULL AFTER `jibun`;

ALTER TABLE `bank_account`
    CHANGE COLUMN `user_id` `user_id` BIGINT NOT NULL AFTER `bank_account_id`,
    CHANGE COLUMN `name` `name` VARCHAR(255) NOT NULL AFTER `user_id`,
    CHANGE COLUMN `number` `number` VARCHAR(255) NOT NULL AFTER `name`;

ALTER TABLE `bid`
    CHANGE COLUMN `bid_id` `bid_id` BIGINT NOT NULL AUTO_INCREMENT FIRST,
    CHANGE COLUMN `user_id` `user_id` BIGINT NOT NULL AFTER `bid_id`,
    CHANGE COLUMN `auction_id` `auction_id` BIGINT NOT NULL AFTER `user_id`,
    CHANGE COLUMN `amount` `amount` BIGINT NOT NULL AFTER `auction_id`,
    CHANGE COLUMN `status` `status` VARCHAR(255) NULL DEFAULT NULL AFTER `count`;

ALTER TABLE `like_table`
    CHANGE COLUMN `user_id` `user_id` BIGINT NOT NULL AFTER `like_id`,
    CHANGE COLUMN `created_at` `created_at` DATETIME(6) NULL DEFAULT NULL AFTER `product_id`;

ALTER TABLE `chzzdb`.`payment`
    CHANGE COLUMN `auction_id` `auction_id` BIGINT NOT NULL AFTER `user_id`,
    CHANGE COLUMN `amount` `amount` BIGINT NOT NULL AFTER `order_id`,
    CHANGE COLUMN `payment_key` `payment_key` VARCHAR(255) NOT NULL AFTER `status`,
    CHANGE COLUMN `created_at` `created_at` DATETIME(6) NULL DEFAULT NULL AFTER `payment_key`,
    CHANGE COLUMN `updated_at` `updated_at` DATETIME(6) NULL DEFAULT NULL AFTER `created_at`;
