CREATE TABLE auction
(
    auction_id    BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime              NULL,
    updated_at    datetime              NULL,
    seller_id     BIGINT                NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    `description` VARCHAR(1000)         NULL,
    min_price     INT                   NULL,
    category      VARCHAR(30)           NOT NULL,
    end_date_time datetime              NULL,
    status        VARCHAR(20)           NULL,
    winner_id     BIGINT                NULL,
    like_count    BIGINT                NULL,
    bid_count     BIGINT                NULL,
    CONSTRAINT pk_auction PRIMARY KEY (auction_id)
);

CREATE TABLE bid
(
    bid_id     BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    bidder_id  BIGINT                NOT NULL,
    auction_id BIGINT                NOT NULL,
    amount     BIGINT                NOT NULL,
    count      INT DEFAULT 2         NOT NULL,
    status     VARCHAR(255)          NULL,
    CONSTRAINT pk_bid PRIMARY KEY (bid_id)
);

CREATE TABLE delivery
(
    delivery_id    BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime              NULL,
    updated_at     datetime              NULL,
    user_id        BIGINT                NOT NULL,
    road_address   VARCHAR(255)          NOT NULL,
    jibun          VARCHAR(255)          NOT NULL,
    zipcode        VARCHAR(255)          NOT NULL,
    detail_address VARCHAR(255)          NOT NULL,
    recipient_name VARCHAR(255)          NOT NULL,
    phone_number   VARCHAR(255)          NOT NULL,
    is_default     BIT(1)                NOT NULL,
    CONSTRAINT pk_delivery PRIMARY KEY (delivery_id)
);

CREATE TABLE image
(
    image_id   BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    auction_id BIGINT                NULL,
    cdn_path   VARCHAR(255)          NOT NULL,
    sequence   INT                   NULL,
    CONSTRAINT pk_image PRIMARY KEY (image_id)
);

CREATE TABLE likes
(
    like_id    BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    user_id    BIGINT                NOT NULL,
    auction_id BIGINT                NOT NULL,
    CONSTRAINT pk_likes PRIMARY KEY (like_id)
);

CREATE TABLE notification
(
    notification_id BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime              NULL,
    updated_at      datetime              NULL,
    user_id         BIGINT                NOT NULL,
    cdn_path        VARCHAR(255)          NULL,
    message         VARCHAR(255)          NOT NULL,
    is_read         BIT(1)                NOT NULL,
    is_deleted      BIT(1)                NOT NULL,
    type            VARCHAR(255)          NULL,
    auction_id      BIGINT                NULL,
    CONSTRAINT pk_notification PRIMARY KEY (notification_id)
);

CREATE TABLE orders
(
    order_id       BIGINT AUTO_INCREMENT NOT NULL,
    order_no       VARCHAR(255)          NOT NULL,
    buyer_id       BIGINT                NOT NULL,
    payment_id     BIGINT                NOT NULL,
    amount         BIGINT                NOT NULL,
    delivery_memo  VARCHAR(255)          NULL,
    road_address   VARCHAR(255)          NOT NULL,
    jibun          VARCHAR(255)          NOT NULL,
    zipcode        VARCHAR(255)          NOT NULL,
    detail_address VARCHAR(255)          NOT NULL,
    recipient_name VARCHAR(255)          NOT NULL,
    phone_number   VARCHAR(255)          NOT NULL,
    method         VARCHAR(30)           NOT NULL,
    auction_id     BIGINT                NULL,
    CONSTRAINT pk_orders PRIMARY KEY (order_id)
);

CREATE TABLE payment
(
    payment_id  BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime              NULL,
    updated_at  datetime              NULL,
    user_id     BIGINT                NOT NULL,
    auction_id  BIGINT                NOT NULL,
    amount      BIGINT                NOT NULL,
    method      VARCHAR(30)           NOT NULL,
    status      VARCHAR(30)           NOT NULL,
    order_no    VARCHAR(255)          NOT NULL,
    payment_key VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_payment PRIMARY KEY (payment_id)
);

CREATE TABLE users
(
    user_id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at        datetime              NULL,
    updated_at        datetime              NULL,
    provider_id       VARCHAR(255)          NOT NULL,
    nickname          VARCHAR(25)           NULL,
    email             VARCHAR(255)          NOT NULL,
    bio               TEXT                  NULL,
    profile_image_url VARCHAR(255)          NULL,
    user_role         VARCHAR(20)           NULL,
    provider_type     VARCHAR(20)           NULL,
    customer_key      BINARY(16)            NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id)
);

ALTER TABLE likes
    ADD CONSTRAINT uc_1f2d6acc739712581f2945dac UNIQUE (user_id, auction_id);

ALTER TABLE payment
    ADD CONSTRAINT uc_payment_orderno UNIQUE (order_no);

ALTER TABLE users
    ADD CONSTRAINT uc_users_customerkey UNIQUE (customer_key);

CREATE INDEX idx_auction_image ON image (image_id, cdn_path, auction_id);

ALTER TABLE auction
    ADD CONSTRAINT FK_AUCTION_ON_SELLER FOREIGN KEY (seller_id) REFERENCES users (user_id);

ALTER TABLE delivery
    ADD CONSTRAINT FK_DELIVERY_ON_USER FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE image
    ADD CONSTRAINT FK_IMAGE_ON_AUCTION FOREIGN KEY (auction_id) REFERENCES auction (auction_id);

ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_AUCTION FOREIGN KEY (auction_id) REFERENCES auction (auction_id);

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_AUCTION FOREIGN KEY (auction_id) REFERENCES auction (auction_id);

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_USER FOREIGN KEY (user_id) REFERENCES users (user_id);


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
    KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`, `REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`, `JOB_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

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
    KEY `IDX_QRTZ_T_J` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_T_C` (`SCHED_NAME`, `CALENDAR_NAME`),
    KEY `IDX_QRTZ_T_G` (`SCHED_NAME`, `TRIGGER_GROUP`),
    KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`, `TRIGGER_GROUP`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`, `NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`, `TRIGGER_STATE`, `NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`, `MISFIRE_INSTR`, `NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`, `MISFIRE_INSTR`, `NEXT_FIRE_TIME`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`, `MISFIRE_INSTR`, `NEXT_FIRE_TIME`, `TRIGGER_GROUP`,
                                         `TRIGGER_STATE`),
    CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS`
(
    `SCHED_NAME`    varchar(120) NOT NULL,
    `TRIGGER_NAME`  varchar(190) NOT NULL,
    `TRIGGER_GROUP` varchar(190) NOT NULL,
    `BLOB_DATA`     blob,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY `SCHED_NAME` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS`
(
    `SCHED_NAME`    varchar(120) NOT NULL,
    `CALENDAR_NAME` varchar(190) NOT NULL,
    `CALENDAR`      blob         NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

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
    KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`, `INSTANCE_NAME`),
    KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`, `INSTANCE_NAME`, `REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS`
(
    `SCHED_NAME` varchar(120) NOT NULL,
    `LOCK_NAME`  varchar(40)  NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS`
(
    `SCHED_NAME`    varchar(120) NOT NULL,
    `TRIGGER_GROUP` varchar(190) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE`
(
    `SCHED_NAME`        varchar(120) NOT NULL,
    `INSTANCE_NAME`     varchar(190) NOT NULL,
    `LAST_CHECKIN_TIME` bigint       NOT NULL,
    `CHECKIN_INTERVAL`  bigint       NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
