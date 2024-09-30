DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `user_id`       bigint       NOT NULL AUTO_INCREMENT,
    `nickname`      varchar(25)  DEFAULT NULL,
    `email`         varchar(255) NOT NULL,
    `bio`           text,
    `link`          varchar(255) DEFAULT NULL,
    `provider_id`   varchar(255) NOT NULL,
    `provider_type` varchar(20)  DEFAULT NULL,
    `customer_key`  binary(16) NOT NULL,
    `user_role`     varchar(20)  DEFAULT NULL,
    `created_at`    datetime(6) DEFAULT NULL,
    `updated_at`    datetime(6) DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `UK_tjpwcsm4fvnedy6uimbl9g8mm` (`customer_key`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`
(
    `product_id`  bigint       NOT NULL AUTO_INCREMENT,
    `user_id`     bigint       NOT NULL,
    `name`        varchar(255) NOT NULL,
    `description` varchar(1000) DEFAULT NULL,
    `category`    varchar(30)  NOT NULL,
    `min_price`   int           DEFAULT NULL,
    `created_at`  datetime(6) DEFAULT NULL,
    `updated_at`  datetime(6) DEFAULT NULL,
    PRIMARY KEY (`product_id`),
    KEY           `idx_product_id_name` (`product_id`,`name`),
    KEY           `FK47nyv78b35eaufr6aa96vep6n` (`user_id`),
    CONSTRAINT `FK47nyv78b35eaufr6aa96vep6n` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `auction`;
CREATE TABLE `auction`
(
    `auction_id`    bigint NOT NULL AUTO_INCREMENT,
    `product_id`    bigint      DEFAULT NULL,
    `status`        varchar(20) DEFAULT NULL,
    `end_date_time` datetime(6) DEFAULT NULL,
    `winner_id`     bigint      DEFAULT NULL,
    `created_at`    datetime(6) DEFAULT NULL,
    `updated_at`    datetime(6) DEFAULT NULL,
    PRIMARY KEY (`auction_id`),
    UNIQUE KEY `UK_kofsgcp79eu3d1puixs92584u` (`product_id`),
    KEY             `idx_auction_end_date_time` (`end_date_time`),
    CONSTRAINT `FKik2rw5as7p6r3y92mlu2hbrrj` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `image`;
CREATE TABLE `image`
(
    `image_id`   bigint       NOT NULL AUTO_INCREMENT,
    `product_id` bigint DEFAULT NULL,
    `cdn_path`   varchar(255) NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`image_id`),
    KEY          `IDXn5mhtpce0785mrnv50axnhlj2` (`product_id`,`image_id`,`cdn_path`),
    CONSTRAINT `FKgpextbyee3uk9u6o2381m7ft1` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`
(
    `notification_id` bigint       NOT NULL AUTO_INCREMENT,
    `user_id`         bigint       NOT NULL,
    `image_id`        bigint DEFAULT NULL,
    `auction_id`      bigint DEFAULT NULL,
    `type`            varchar(31)  NOT NULL,
    `message`         varchar(255) NOT NULL,
    `is_read`         bit(1)       NOT NULL,
    `is_deleted`      bit(1)       NOT NULL,
    `created_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    PRIMARY KEY (`notification_id`),
    KEY               `FKholipoc9p58ukvigqmd8ejvoo` (`image_id`),
    KEY               `FKnk4ftb5am9ubmkv1661h15ds9` (`user_id`),
    CONSTRAINT `FKholipoc9p58ukvigqmd8ejvoo` FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`),
    CONSTRAINT `FKnk4ftb5am9ubmkv1661h15ds9` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`
(
    `address_id`     bigint NOT NULL AUTO_INCREMENT,
    `user_id`        bigint NOT NULL,
    `zipcode`        varchar(255) DEFAULT NULL,
    `road_address`   varchar(255) DEFAULT NULL,
    `jibun`          varchar(255) DEFAULT NULL,
    `detail_address` varchar(255) DEFAULT NULL,
    `created_at`     datetime(6) DEFAULT NULL,
    `updated_at`     datetime(6) DEFAULT NULL,
    PRIMARY KEY (`address_id`),
    KEY              `FK6i66ijb8twgcqtetl8eeeed6v` (`user_id`),
    CONSTRAINT `FK6i66ijb8twgcqtetl8eeeed6v` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `bank_account`;
CREATE TABLE `bank_account`
(
    `bank_account_id` bigint       NOT NULL AUTO_INCREMENT,
    `user_id`         bigint       NOT NULL,
    `name`            varchar(255) NOT NULL,
    `number`          varchar(255) NOT NULL,
    `created_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    PRIMARY KEY (`bank_account_id`),
    KEY               `FKftsfxon3d4ectm5bv7glrhlko` (`user_id`),
    CONSTRAINT `FKftsfxon3d4ectm5bv7glrhlko` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `bid`;
CREATE TABLE `bid`
(
    `bid_id`     bigint NOT NULL AUTO_INCREMENT,
    `user_id`    bigint NOT NULL,
    `auction_id` bigint NOT NULL,
    `amount`     bigint NOT NULL,
    `count`      int    NOT NULL DEFAULT '3',
    `status`     varchar(255)    DEFAULT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`bid_id`),
    KEY          `FKhexc6i4j8i0tmpt8bdulp6g3g` (`auction_id`),
    KEY          `FKi1pwg1muxilapowsmifod8jtf` (`user_id`),
    CONSTRAINT `FKhexc6i4j8i0tmpt8bdulp6g3g` FOREIGN KEY (`auction_id`) REFERENCES `auction` (`auction_id`),
    CONSTRAINT `FKi1pwg1muxilapowsmifod8jtf` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `like_table`;
CREATE TABLE `like_table`
(
    `like_id`    bigint NOT NULL AUTO_INCREMENT,
    `user_id`    bigint NOT NULL,
    `product_id` bigint NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`like_id`),
    UNIQUE KEY `UKspmgcymhkuyqi5k8jb3k597kn` (`user_id`,`product_id`),
    KEY          `FK5q2gfd8rptdrkftmoqje3jjbw` (`product_id`),
    CONSTRAINT `FK1iv11yge276b5tut7r0151m98` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT `FK5q2gfd8rptdrkftmoqje3jjbw` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`
(
    `payment_id`  bigint       NOT NULL AUTO_INCREMENT,
    `user_id`     bigint       NOT NULL,
    `auction_id`  bigint       NOT NULL,
    `order_id`    varchar(255) NOT NULL,
    `amount`      bigint       NOT NULL,
    `method`      varchar(30)  NOT NULL,
    `status`      varchar(30)  NOT NULL,
    `payment_key` varchar(255) NOT NULL,
    `created_at`  datetime(6) DEFAULT NULL,
    `updated_at`  datetime(6) DEFAULT NULL,
    PRIMARY KEY (`payment_id`),
    UNIQUE KEY `UK_mf7n8wo2rwrxsd6f3t9ub2mep` (`order_id`),
    KEY           `FKb0ekvs48lsday0ohucw8a1yi` (`auction_id`),
    KEY           `FKmi2669nkjesvp7cd257fptl6f` (`user_id`),
    CONSTRAINT `FKb0ekvs48lsday0ohucw8a1yi` FOREIGN KEY (`auction_id`) REFERENCES `auction` (`auction_id`),
    CONSTRAINT `FKmi2669nkjesvp7cd257fptl6f` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

# ------------------------------------------------------------------------------------------------------------------------

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS`
(
    `SCHED_NAME`        varchar(120) NOT NULL,
    `JOB_NAME`          varchar(190) NOT NULL,
    `JOB_GROUP`         varchar(190) NOT NULL,
    `DESCRIPTION`       varchar(250) DEFAULT NULL,
    `JOB_CLASS_NAME`    varchar(250) NOT NULL,
    `IS_DURABLE`        varchar(1)   NOT NULL,
    `IS_NONCONCURRENT`  varchar(1)   NOT NULL,
    `IS_UPDATE_DATA`    varchar(1)   NOT NULL,
    `REQUESTS_RECOVERY` varchar(1)   NOT NULL,
    `JOB_DATA`          blob,
    PRIMARY KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`),
    KEY                 `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
    KEY                 `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS`
(
    `SCHED_NAME`     varchar(120) NOT NULL,
    `TRIGGER_NAME`   varchar(190) NOT NULL,
    `TRIGGER_GROUP`  varchar(190) NOT NULL,
    `JOB_NAME`       varchar(190) NOT NULL,
    `JOB_GROUP`      varchar(190) NOT NULL,
    `DESCRIPTION`    varchar(250) DEFAULT NULL,
    `NEXT_FIRE_TIME` bigint       DEFAULT NULL,
    `PREV_FIRE_TIME` bigint       DEFAULT NULL,
    `PRIORITY`       int          DEFAULT NULL,
    `TRIGGER_STATE`  varchar(16)  NOT NULL,
    `TRIGGER_TYPE`   varchar(8)   NOT NULL,
    `START_TIME`     bigint       NOT NULL,
    `END_TIME`       bigint       DEFAULT NULL,
    `CALENDAR_NAME`  varchar(190) DEFAULT NULL,
    `MISFIRE_INSTR`  smallint     DEFAULT NULL,
    `JOB_DATA`       blob,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY              `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
    KEY              `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
    KEY              `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
    KEY              `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
    KEY              `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
    KEY              `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
    KEY              `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
    KEY              `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
    KEY              `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
    KEY              `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
    KEY              `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
    KEY              `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
    CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS`
(
    `SCHED_NAME`    varchar(120) NOT NULL,
    `TRIGGER_NAME`  varchar(190) NOT NULL,
    `TRIGGER_GROUP` varchar(190) NOT NULL,
    `BLOB_DATA`     blob,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY             `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS`
(
    `SCHED_NAME`    varchar(120) NOT NULL,
    `CALENDAR_NAME` varchar(190) NOT NULL,
    `CALENDAR`      blob         NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS`
(
    `SCHED_NAME`      varchar(120) NOT NULL,
    `TRIGGER_NAME`    varchar(190) NOT NULL,
    `TRIGGER_GROUP`   varchar(190) NOT NULL,
    `CRON_EXPRESSION` varchar(120) NOT NULL,
    `TIME_ZONE_ID`    varchar(80) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS`
(
    `SCHED_NAME`        varchar(120) NOT NULL,
    `ENTRY_ID`          varchar(95)  NOT NULL,
    `TRIGGER_NAME`      varchar(190) NOT NULL,
    `TRIGGER_GROUP`     varchar(190) NOT NULL,
    `INSTANCE_NAME`     varchar(190) NOT NULL,
    `FIRED_TIME`        bigint       NOT NULL,
    `SCHED_TIME`        bigint       NOT NULL,
    `PRIORITY`          int          NOT NULL,
    `STATE`             varchar(16)  NOT NULL,
    `JOB_NAME`          varchar(190) DEFAULT NULL,
    `JOB_GROUP`         varchar(190) DEFAULT NULL,
    `IS_NONCONCURRENT`  varchar(1)   DEFAULT NULL,
    `REQUESTS_RECOVERY` varchar(1)   DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `ENTRY_ID`),
    KEY                 `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
    KEY                 `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
    KEY                 `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
    KEY                 `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
    KEY                 `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    KEY                 `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS`
(
    `SCHED_NAME` varchar(120) NOT NULL,
    `LOCK_NAME`  varchar(40)  NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS`
(
    `SCHED_NAME`    varchar(120) NOT NULL,
    `TRIGGER_GROUP` varchar(190) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE`
(
    `SCHED_NAME`        varchar(120) NOT NULL,
    `INSTANCE_NAME`     varchar(190) NOT NULL,
    `LAST_CHECKIN_TIME` bigint       NOT NULL,
    `CHECKIN_INTERVAL`  bigint       NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS`
(
    `SCHED_NAME`      varchar(120) NOT NULL,
    `TRIGGER_NAME`    varchar(190) NOT NULL,
    `TRIGGER_GROUP`   varchar(190) NOT NULL,
    `REPEAT_COUNT`    bigint       NOT NULL,
    `REPEAT_INTERVAL` bigint       NOT NULL,
    `TIMES_TRIGGERED` bigint       NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS`
(
    `SCHED_NAME`    varchar(120) NOT NULL,
    `TRIGGER_NAME`  varchar(190) NOT NULL,
    `TRIGGER_GROUP` varchar(190) NOT NULL,
    `STR_PROP_1`    varchar(512)   DEFAULT NULL,
    `STR_PROP_2`    varchar(512)   DEFAULT NULL,
    `STR_PROP_3`    varchar(512)   DEFAULT NULL,
    `INT_PROP_1`    int            DEFAULT NULL,
    `INT_PROP_2`    int            DEFAULT NULL,
    `LONG_PROP_1`   bigint         DEFAULT NULL,
    `LONG_PROP_2`   bigint         DEFAULT NULL,
    `DEC_PROP_1`    decimal(13, 4) DEFAULT NULL,
    `DEC_PROP_2`    decimal(13, 4) DEFAULT NULL,
    `BOOL_PROP_1`   varchar(1)     DEFAULT NULL,
    `BOOL_PROP_2`   varchar(1)     DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
