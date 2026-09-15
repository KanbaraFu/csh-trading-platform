-- =============================================================================
-- 校园二手交易平台 —— 建表脚本
-- 数据库：campus_secondhand
--
-- 说明：
--   1. 本文件由 SpringBoot 启动时自动执行（spring.sql.init），也可手动导入：
--        mysql -u root -p campus_secondhand < schema.sql
--   2. 全部使用 CREATE TABLE IF NOT EXISTS，重复执行不会报错、不会清空已有数据。
--   3. 索引一律内联在建表语句里（MySQL 不支持 CREATE INDEX IF NOT EXISTS），
--      因此请勿把索引拆成独立的 CREATE INDEX 语句。
--   4. 若某张表已存在但字段和这里不一致，IF NOT EXISTS 不会帮你补字段，
--      需要 DROP 该表后重跑，或手动 ALTER。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `username`    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号，本项目用手机号',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码密文，请勿存明文',
  `nickname`    VARCHAR(50),
  `avatar`      VARCHAR(255),
  `phone`       VARCHAR(20),
  `gender`      TINYINT      DEFAULT 0 COMMENT '0未知 1男 2女',
  `status`      TINYINT      DEFAULT 1 COMMENT '0禁用 1正常',
  -- 扩展字段：前端「个人资料页」已依赖，删除前请先确认前端改版
  `student_no`  VARCHAR(20)  COMMENT '学号，用于学生身份认证',
  `college`     VARCHAR(50)  COMMENT '学院，商品详情页展示卖家学院',
  `auth_status` TINYINT      DEFAULT 0 COMMENT '0未认证 1已认证学生',
  `bio`         VARCHAR(255) COMMENT '个人简介',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_user_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户';

-- -----------------------------------------------------------------------------
-- 2. 收货地址表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `address` (
  `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `user_id`       BIGINT       NOT NULL,
  `receiver_name` VARCHAR(50)  NOT NULL,
  `phone`         VARCHAR(20)  NOT NULL,
  `region`        VARCHAR(100) COMMENT '校区 / 宿舍楼等大区域',
  `detail`        VARCHAR(255) COMMENT '详细门牌',
  `is_default`    TINYINT      DEFAULT 0 COMMENT '0否 1默认',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_address_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收货地址';

-- -----------------------------------------------------------------------------
-- 3. 分类表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT      PRIMARY KEY AUTO_INCREMENT,
  `name`        VARCHAR(50) NOT NULL,
  `sort`        INT         DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status`      TINYINT     DEFAULT 1 COMMENT '0停用 1启用',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类';

-- -----------------------------------------------------------------------------
-- 4. 商品表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `product` (
  `id`             BIGINT         PRIMARY KEY AUTO_INCREMENT,
  `seller_id`      BIGINT         NOT NULL,
  `category_id`    BIGINT         NOT NULL,
  `title`          VARCHAR(100)   NOT NULL,
  `description`    TEXT,
  `price`          DECIMAL(10, 2) NOT NULL COMMENT '现价',
  `original_price` DECIMAL(10, 2) COMMENT '原价',
  `stock`          INT            DEFAULT 1,
  `cover`          VARCHAR(255),
  `status`         TINYINT        DEFAULT 1 COMMENT '0下架 1在售 2已售',
  `view_count`     INT            DEFAULT 0,
  `sales_count`    INT            DEFAULT 0,
  -- 扩展字段：前端「成色 / 交易地点」已依赖，删除前请先确认前端改版
  `condition`      VARCHAR(20) COMMENT '成色，取值见前端 constants/product.js CONDITION_OPTIONS。注意：condition 是 MySQL 保留字，实体需写 @TableField(value = "`condition`")',
  `location`       VARCHAR(50) COMMENT '交易地点',
  `create_time`    DATETIME       DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_product_category_status` (`category_id`, `status`, `create_time`),
  KEY `idx_product_seller_status` (`seller_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品';

-- -----------------------------------------------------------------------------
-- 5. 商品图片表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `product_image` (
  `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `product_id` BIGINT       NOT NULL,
  `url`        VARCHAR(255) NOT NULL,
  `sort`       INT          DEFAULT 0,
  KEY `idx_product_image_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品图片';

-- -----------------------------------------------------------------------------
-- 6. 收藏表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorite` (
  `id`          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `product_id`  BIGINT   NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_favorite_user_product` (`user_id`, `product_id`),
  KEY `idx_favorite_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收藏';

-- -----------------------------------------------------------------------------
-- 7. 购物车表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cart` (
  `id`          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `product_id`  BIGINT   NOT NULL,
  `quantity`    INT      DEFAULT 1,
  `selected`    TINYINT  DEFAULT 1 COMMENT '0未选中 1选中',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_cart_user_product` (`user_id`, `product_id`),
  KEY `idx_cart_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '购物车';

-- -----------------------------------------------------------------------------
-- 8. 订单表
--    order 是 MySQL 保留字，实体必须写成 @TableName(value = "`order`")
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order` (
  `id`               BIGINT         PRIMARY KEY AUTO_INCREMENT,
  `order_no`         VARCHAR(50)    NOT NULL UNIQUE,
  `buyer_id`         BIGINT         NOT NULL,
  `seller_id`        BIGINT         NOT NULL,
  `total_amount`     DECIMAL(10, 2) NOT NULL,
  `pay_amount`       DECIMAL(10, 2) NOT NULL,
  `status`           TINYINT        DEFAULT 0 COMMENT '0待支付 1已支付 2已发货 3已完成 4已取消',
  `address_snapshot` VARCHAR(500)   COMMENT '下单时的收货地址快照',
  `pay_time`         DATETIME,
  -- 扩展字段：前端「订单详情 / 下单备注」已依赖
  `remark`           VARCHAR(255)   COMMENT '买家下单备注',
  `pay_method`       VARCHAR(20)    COMMENT 'campus_card / wechat / alipay，模拟支付',
  `create_time`      DATETIME       DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_order_buyer_status` (`buyer_id`, `status`),
  KEY `idx_order_seller_status` (`seller_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单';

-- -----------------------------------------------------------------------------
-- 9. 订单明细表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_item` (
  `id`            BIGINT         PRIMARY KEY AUTO_INCREMENT,
  `order_id`      BIGINT         NOT NULL,
  `product_id`    BIGINT         NOT NULL,
  `product_title` VARCHAR(100)   COMMENT '下单时的商品标题快照',
  `product_cover` VARCHAR(255)   COMMENT '下单时的商品封面快照',
  `price`         DECIMAL(10, 2),
  `quantity`      INT,
  `total_amount`  DECIMAL(10, 2),
  KEY `idx_order_item_order` (`order_id`),
  KEY `idx_order_item_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单明细';

-- -----------------------------------------------------------------------------
-- 10. 评论表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
  `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `product_id`    BIGINT       NOT NULL,
  `user_id`       BIGINT       NOT NULL,
  `content`       VARCHAR(500) NOT NULL,
  `parent_id`     BIGINT       DEFAULT 0 COMMENT '0一级评论，否则为被回复的评论 id',
  `reply_user_id` BIGINT       COMMENT '被回复的用户 id',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_comment_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品评论';

-- -----------------------------------------------------------------------------
-- 11. 消息表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `message` (
  `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL COMMENT '接收者',
  `type`        VARCHAR(50)  COMMENT 'trade交易 / comment评论 / system系统',
  `title`       VARCHAR(100),
  `content`     VARCHAR(500),
  `biz_id`      BIGINT       COMMENT '关联业务 id，如订单 id / 商品 id',
  `is_read`     TINYINT      DEFAULT 0 COMMENT '0未读 1已读',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_message_user_read` (`user_id`, `is_read`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '站内消息';

-- -----------------------------------------------------------------------------
-- 12. 浏览记录表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `browse_log` (
  `id`          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT   COMMENT '可为空，未登录浏览',
  `product_id`  BIGINT   NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_browse_log_user` (`user_id`, `create_time`),
  KEY `idx_browse_log_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '浏览记录';
