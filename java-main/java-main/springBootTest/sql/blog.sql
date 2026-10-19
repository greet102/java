-- =============================================================
-- 喵喵博客（mewblog）数据库初始化脚本
-- 数据库: blog_db    字符集: utf8mb4
-- 使用方法: mysql -uroot -p123456 --default-character-set=utf8mb4 < blog.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS `blog_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `blog_db`;

-- 先删旧表，保证脚本可重复执行
DROP TABLE IF EXISTS `post_like`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `user`;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`   VARCHAR(50)  NOT NULL                COMMENT '用户名',
  `password`   VARCHAR(100) NOT NULL                COMMENT '密码（演示项目，明文存储）',
  `created_at` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 文章表
-- ts 为发布时间的毫秒时间戳，与前端保持一致
-- ------------------------------------------------------------
CREATE TABLE `post` (
  `id`     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `author` VARCHAR(50)   NOT NULL                COMMENT '作者用户名',
  `emoji`  VARCHAR(16)            DEFAULT '🐱'   COMMENT '心情表情',
  `title`  VARCHAR(200)  NOT NULL                COMMENT '标题',
  `body`   TEXT          NOT NULL                COMMENT '正文',
  `ts`     BIGINT        NOT NULL                COMMENT '发布时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_author` (`author`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章表';

-- ------------------------------------------------------------
-- 点赞表（一篇文章一人只能赞一次）
-- ------------------------------------------------------------
CREATE TABLE `post_like` (
  `id`       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id`  BIGINT      NOT NULL                COMMENT '文章ID',
  `username` VARCHAR(50) NOT NULL                COMMENT '点赞用户名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '点赞表';

-- ------------------------------------------------------------
-- 种子数据：用户（密码统一为 123456，仅演示用）
-- ------------------------------------------------------------
INSERT INTO `user` (`username`, `password`) VALUES
('喵站长', '123456'),
('奶油喵', '123456'),
('星星',   '123456');

-- ------------------------------------------------------------
-- 种子数据：文章（与前端原 localStorage 中的演示数据一致）
-- ------------------------------------------------------------
INSERT INTO `post` (`author`, `emoji`, `title`, `body`, `ts`) VALUES
('喵站长', '🌈', '欢迎来到喵喵博客！',
 '这里是一个软软糯糯、五彩斑斓的小天地～\n你可以写心情、晒日常、分享喜欢的一切。\n快来发你的第一篇文章吧！',
 (UNIX_TIMESTAMP(NOW()) - 5  * 3600) * 1000),
('奶油喵', '🍰', '今天吃到了超好吃的蛋糕',
 '草莓味的！甜而不腻，配上一杯热牛奶简直完美。\n生活的小确幸就是这样简单 (๑>ᴗ<๑)',
 (UNIX_TIMESTAMP(NOW()) - 26 * 3600) * 1000),
('星星',   '⭐', '今晚的夜空好亮',
 '加班回家的路上抬头看见好多星星，瞬间被治愈了。\n记得也要好好休息哦～',
 (UNIX_TIMESTAMP(NOW()) - 50 * 3600) * 1000);

-- ------------------------------------------------------------
-- 种子数据：点赞
-- ------------------------------------------------------------
INSERT INTO `post_like` (`post_id`, `username`) VALUES
(1, '喵站长'),
(2, '喵站长'),
(2, '奶油喵'),
(3, '喵站长');
