-- =============================================================
-- 喵喵博客 数据库结构升级脚本（幂等，可重复执行）
-- 用途：在已存在的 blog_db 上补齐 2026-10-23 新增的
--       收藏表 post_favorite、关注关系表 user_follow
--       与 user 表个人主页字段。
--       全新导入请直接用 blog.sql，本脚本仅做增量补齐，
--       不会清空已有数据。
-- 注意：MySQL 8.0 不支持 "ALTER TABLE ... ADD COLUMN IF NOT EXISTS"，
--       因此用存储过程按 information_schema 逐列判断后增量补齐。
-- 使用方法:
--   mysql -uroot -p123456 --default-character-set=utf8mb4 < blog_db_upgrade.sql
-- =============================================================

USE `blog_db`;

-- ------------------------------------------------------------
-- 收藏表（一篇文章一人只能收藏一次）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_favorite` (
  `id`       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `post_id`  BIGINT      NOT NULL                COMMENT '文章ID',
  `username` VARCHAR(50) NOT NULL                COMMENT '收藏用户名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user_fav` (`post_id`, `username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收藏表';

-- ------------------------------------------------------------
-- 关注关系表（follower 关注了 following）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `follower`  VARCHAR(50)  NOT NULL                COMMENT '关注者用户名',
  `following` VARCHAR(50)  NOT NULL                COMMENT '被关注者用户名',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow_pair` (`follower`, `following`),
  KEY `idx_following` (`following`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '关注关系表';

-- ------------------------------------------------------------
-- user 表：补齐个人主页相关字段（按列幂等补齐）
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS `add_user_cols`;
DELIMITER //
CREATE PROCEDURE `add_user_cols`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'blog_db' AND table_name = 'user' AND column_name = 'avatar') THEN
    ALTER TABLE `user` ADD COLUMN `avatar`   VARCHAR(16)  DEFAULT '🐱' COMMENT '头像（用一个表情充当）';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'blog_db' AND table_name = 'user' AND column_name = 'nickname') THEN
    ALTER TABLE `user` ADD COLUMN `nickname` VARCHAR(50)  DEFAULT NULL COMMENT '昵称，为空时回退显示用户名';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'blog_db' AND table_name = 'user' AND column_name = 'slogan') THEN
    ALTER TABLE `user` ADD COLUMN `slogan`   VARCHAR(100) DEFAULT NULL COMMENT '个人主页标语';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'blog_db' AND table_name = 'user' AND column_name = 'bio') THEN
    ALTER TABLE `user` ADD COLUMN `bio`      VARCHAR(500) DEFAULT NULL COMMENT '个人简介';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'blog_db' AND table_name = 'user' AND column_name = 'location') THEN
    ALTER TABLE `user` ADD COLUMN `location` VARCHAR(100) DEFAULT NULL COMMENT '所在地';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'blog_db' AND table_name = 'user' AND column_name = 'birthday') THEN
    ALTER TABLE `user` ADD COLUMN `birthday` DATE         DEFAULT NULL COMMENT '生日';
  END IF;
END //
DELIMITER ;
CALL `add_user_cols`();
DROP PROCEDURE IF EXISTS `add_user_cols`;
