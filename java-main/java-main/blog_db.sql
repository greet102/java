/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : blog_db

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 04/11/2026 09:32:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint NOT NULL COMMENT '文章ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论者用户名',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE,
  INDEX `idx_user`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (5, 1, 'xx', '嘻嘻嘻', '2026-11-04 09:30:54');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送者用户名',
  `receiver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收者用户名',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '接收者是否已读（0未读 1已读）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver`(`receiver` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_pair`(`sender` ASC, `receiver` ASC) USING BTREE,
  INDEX `idx_pair_rev`(`receiver` ASC, `sender` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (4, '咪咪', 'xx', '你好呀', 1, '2026-10-30 16:26:40');
INSERT INTO `message` VALUES (6, 'xx', '咪咪', '你好', 1, '2026-10-30 16:27:29');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作者用户名',
  `emoji` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '?' COMMENT '心情表情',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正文',
  `ts` bigint NOT NULL COMMENT '发布时间（毫秒时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_author`(`author` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (1, '喵站长', '🌈', '欢迎来到喵喵博客！', '这里是一个软软糯糯、五彩斑斓的小天地～\n你可以写心情、晒日常、分享喜欢的一切。\n快来发你的第一篇文章吧！', 1793131864000);
INSERT INTO `post` VALUES (2, '奶油喵', '🍰', '今天吃到了超好吃的蛋糕', '草莓味的！甜而不腻，配上一杯热牛奶简直完美。\n生活的小确幸就是这样简单 (๑>ᴗ<๑)', 1793056264000);
INSERT INTO `post` VALUES (3, '星星', '⭐', '今晚的夜空好亮', '加班回家的路上抬头看见好多星星，瞬间被治愈了。\n记得也要好好休息哦～', 1792969864000);
INSERT INTO `post` VALUES (4, 'xx', '🌈', '嘻嘻', '啦啦啦啦啦', 1793346394205);

-- ----------------------------
-- Table structure for post_favorite
-- ----------------------------
DROP TABLE IF EXISTS `post_favorite`;
CREATE TABLE `post_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `post_id` bigint NOT NULL COMMENT '文章ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收藏用户名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user_fav`(`post_id` ASC, `username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_favorite
-- ----------------------------

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` bigint NOT NULL COMMENT '文章ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点赞用户名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_like
-- ----------------------------
INSERT INTO `post_like` VALUES (1, 1, '喵站长');
INSERT INTO `post_like` VALUES (2, 2, '喵站长');
INSERT INTO `post_like` VALUES (3, 2, '奶油喵');
INSERT INTO `post_like` VALUES (4, 3, '喵站长');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（演示项目，明文存储）',
  `permission` tinyint NOT NULL DEFAULT 1 COMMENT '权限：1=普通用户（默认），0=管理员',
  `avatar` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '?' COMMENT '头像（用一个表情充当）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称，为空时回退显示用户名',
  `slogan` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人主页标语',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在地',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '喵站长', '123456', 0, '🐱', '', '', '', '', NULL, '2026-10-28 09:11:04');
INSERT INTO `user` VALUES (2, '奶油喵', '123456', 1, '🐱', NULL, NULL, NULL, NULL, NULL, '2026-10-28 09:11:04');
INSERT INTO `user` VALUES (3, '星星', '123456', 1, '🐱', NULL, NULL, NULL, NULL, NULL, '2026-10-28 09:11:04');
INSERT INTO `user` VALUES (4, 'xx', '1234', 0, '🐱', 'xx', '这个人很懒，什么也没写', '', '喵星', NULL, '2026-10-28 09:26:45');
INSERT INTO `user` VALUES (10, '咪咪', '1111', 1, '🐱', '咪咪', '这个人很懒，什么也没写～', '', '', NULL, '2026-10-30 16:26:21');

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `follower` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关注者用户名',
  `following` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '被关注者用户名',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follow_pair`(`follower` ASC, `following` ASC) USING BTREE,
  INDEX `idx_following`(`following` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '关注关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_follow
-- ----------------------------
INSERT INTO `user_follow` VALUES (1, '喵站长', '奶油喵', '2026-10-28 09:11:04');
INSERT INTO `user_follow` VALUES (3, '喵站长', '星星', '2026-10-28 09:12:54');
INSERT INTO `user_follow` VALUES (5, 'xx', '喵站长', '2026-10-28 09:27:50');
INSERT INTO `user_follow` VALUES (6, 'xx', '奶油喵', '2026-10-28 09:28:06');
INSERT INTO `user_follow` VALUES (9, '咪咪', 'xx', '2026-10-30 16:26:27');

SET FOREIGN_KEY_CHECKS = 1;
