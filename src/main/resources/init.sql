-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: Dpoker
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `point` float NOT NULL DEFAULT '0' COMMENT '用户积分',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `test` tinyint(1) DEFAULT 0 COMMENT '是否为测试账号 0:否 1:是',
  `nickname` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像标识 preset:N，NULL 时前端按昵称 hash 兜底',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'alice','123456',10000,'2025-03-10 14:30:00',1,'alice','preset:0'),(2,'bob','password',10000,'2025-04-01 09:15:22',1,'bob','preset:1'),(3,'charlie','qwerty123',10000,'2024-12-25 18:45:10',1,'charlie','preset:2'),(4,'李四','lisi123',10000,'2026-01-18 10:00:00',1,'李四','preset:3'),(5,'eve','letmein',10000,'2026-01-18 15:04:20',1,'eve','preset:4'),(6,'ds','616',39610,'2026-01-22 20:43:51',0,'戴爷','preset:5'),(7,'pyq','616',-70000,'2026-01-22 20:43:51',0,'东北王','preset:6'),(8,'xg','616',55802,'2026-01-22 20:43:51',0,'叉寄','preset:7'),(9,'zxy','616',20164,'2026-01-22 20:43:51',0,'周星星','preset:8'),(10,'zy','616',24424,'2026-01-22 20:43:51',0,'综艺','preset:9'),(11,'地味','616',10000,'2026-01-22 20:43:51',0,'地位','preset:10'),(12,'bjz','616',-10000,'2026-01-22 20:43:51',0,'鲍jiezhi','preset:11');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `season`
-- 赛季表：每赛季一条记录，status='active' 的为当前赛季（全表唯一）
--

DROP TABLE IF EXISTS `season`;
CREATE TABLE `season` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `season_number` int NOT NULL COMMENT '赛季编号 1,2,3...',
  `start_time` datetime NOT NULL COMMENT '赛季开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '赛季结束时间，当前赛季为 NULL',
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT 'active=进行中 / ended=已结束',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`) COMMENT '普通索引，active 唯一性由应用层结算逻辑保证'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛季记录表';

-- 初始赛季：赛季 1，进行中
INSERT INTO `season` VALUES (1,1,'2026-01-01 00:00:00',NULL,'active');

--
-- Table structure for table `season_rank`
-- 赛季排名快照表：结算时把每个玩家最终积分写入此表
--

DROP TABLE IF EXISTS `season_rank`;
CREATE TABLE `season_rank` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `season_id` int NOT NULL COMMENT '赛季ID，关联 season.id',
  `user_id` int NOT NULL COMMENT '用户ID',
  `nickname` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结算时昵称快照',
  `avatar` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '结算时头像快照',
  `final_point` float NOT NULL COMMENT '最终积分',
  `rank` int NOT NULL COMMENT '名次 1,2,3...',
  PRIMARY KEY (`id`),
  KEY `idx_season_id` (`season_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛季排名快照表';
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-18 20:39:20
