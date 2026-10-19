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

 Date: 14/10/2026 08:50:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (1, '喵站长', '🌈', '欢迎来到喵喵博客！', '这里是一个软软糯糯、五彩斑斓的小天地～\n你可以写心情、晒日常、分享喜欢的一切。\n快来发你的第一篇文章吧！', 1791920704000);
INSERT INTO `post` VALUES (2, '奶油喵', '🍰', '今天吃到了超好吃的蛋糕', '草莓味的！甜而不腻，配上一杯热牛奶简直完美。\n生活的小确幸就是这样简单 (๑>ᴗ<๑)', 1791845104000);
INSERT INTO `post` VALUES (3, '星星', '⭐', '今晚的夜空好亮', '加班回家的路上抬头看见好多星星，瞬间被治愈了。\n记得也要好好休息哦～', 1791758704000);

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

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
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '喵站长', '123456', '2026-10-14 08:45:04');
INSERT INTO `user` VALUES (2, '奶油喵', '123456', '2026-10-14 08:45:04');
INSERT INTO `user` VALUES (3, '星星', '123456', '2026-10-14 08:45:04');

SET FOREIGN_KEY_CHECKS = 1;
