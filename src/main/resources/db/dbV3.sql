-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: trackerfinancesecuritydbv3
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `current_balance` double NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,164,'EFECTIVO',1),(2,590,'BBVA',1),(3,0,'BCP',1),(4,5000,'SAGA FALABELLA',1),(5,70,'PREXPE',1),(6,1520,'NEW ACCOUNT',1),(8,15005,'TEST144',1),(9,1000,'NUEVA CUENTA',1),(10,2000,'NUEVA',1),(11,6300,'POIPIO',1),(12,2000,'WWW',1),(13,60,'TEST1',1),(14,1600,'test 2',1),(15,900,'7897897',1),(16,600,'CUENTA 1',1),(17,320,'billetera',1),(18,5015,'ghjghjgj',1),(19,1800,'nuevo 5',1),(20,746,'kkkkkk',1),(21,100,'TEST2',1);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'prestamos',1),(2,'ALQUILER',1),(3,'PENSION',1),(4,'MERCADO',1),(5,'NETFLIX',1),(6,'CRUNCHYROLL',1),(7,'PRIME VIDEO',1),(8,'SPOTIFY',1),(9,'GUSTITOS',1),(10,'MOVILIDAD',1),(11,'CABLE VISION STA ANITA',1),(12,'FAMILIA',1),(13,'SALIDA AMIGOS',1),(14,'SALIDA FAMILIA',1),(15,'SERVICIOS AGUA',1),(16,'SERVICIOS LUZ',1),(17,'SERVICIOS GAS',1),(18,'SERVICIOS GAS BALON',1),(19,'MOVIL EVER',1),(20,'OTROS',1),(21,'',1),(22,'CATEGORY TEST',1),(23,'CATEGPRY TEST 2',1),(24,'CATEG TEST 3',1),(25,'NEW CATEG',1),(26,'RECREACIÓN',1);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'cinthia.aries.154@gmail.com','cinthia eduardo',1),(2,'','Juan ciseron',1),(3,'','cesar rosario',1);
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_method`
--

DROP TABLE IF EXISTS `payment_method`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_method` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK63xr2pgmapw8eecpfwmfoas7l` (`account_id`),
  CONSTRAINT `FK63xr2pgmapw8eecpfwmfoas7l` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_method`
--

LOCK TABLES `payment_method` WRITE;
/*!40000 ALTER TABLE `payment_method` DISABLE KEYS */;
INSERT INTO `payment_method` VALUES (1,'EFECTIVO',1),(2,'BCP CARD',3),(3,'BCP APP 3',3),(4,'YAPE',3),(5,'PLIN',2),(6,'BBVA CARD',2),(7,'BBVA APP',2),(8,'SAGA APP',4),(9,'SAGA CARD',4),(10,'PREXPE APP',5),(11,'PREXPE CARD',5),(12,'HJKHJK',8),(13,'MEDIO DEPAGO NUEVO',13),(14,'OTR MÁS',17),(15,'NUENO MEDIO PAGO',18),(16,'NUEVO3',19),(17,'YYUYU',20),(18,'PM TEST 1',8);
/*!40000 ALTER TABLE `payment_method` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `segment`
--

DROP TABLE IF EXISTS `segment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `segment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `segment`
--

LOCK TABLES `segment` WRITE;
/*!40000 ALTER TABLE `segment` DISABLE KEYS */;
INSERT INTO `segment` VALUES (1,'FIJO',1),(2,'VARIABLE',1),(3,'NUEVO SEGMENT',1),(4,'NUEVO 2',1),(5,'RTYRY',1),(6,'SEGMENT NEW',1),(7,'ERTE',1);
/*!40000 ALTER TABLE `segment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('RECIBÍ','REALICÉ','NOT_APPLICABLE') DEFAULT NULL,
  `amount` double NOT NULL,
  `block` enum('IN','OUT','NOT_APPLICABLE') DEFAULT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `id_loan_assoc` bigint DEFAULT NULL,
  `remaining` double NOT NULL,
  `status` enum('PENDING','PAYED','NOT_APPLICABLE') DEFAULT NULL,
  `type` enum('LOAN','PAYMENT','EXPENSE','TRANSFERENCE','INCOME') DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `payment_method_id` bigint DEFAULT NULL,
  `payment_method_destiny_id` bigint DEFAULT NULL,
  `segment_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgik7ruym8r1n4xngrclc6kiih` (`category_id`),
  KEY `FK8g210cmyp5dyikvxf9ek6ub30` (`member_id`),
  KEY `FK3iabppaif3d2a8jjotnly31j2` (`payment_method_id`),
  KEY `FK3iabppaif3d2a8jjotnly31j3` (`payment_method_destiny_id`),
  KEY `FKa7apm3ouijiwo82vd76yjclqr` (`segment_id`),
  CONSTRAINT `FK3iabppaif3d2a8jjotnly31j2` FOREIGN KEY (`payment_method_id`) REFERENCES `payment_method` (`id`),
  CONSTRAINT `FK3iabppaif3d2a8jjotnly31j3` FOREIGN KEY (`payment_method_destiny_id`) REFERENCES `payment_method` (`id`),
  CONSTRAINT `FK8g210cmyp5dyikvxf9ek6ub30` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKa7apm3ouijiwo82vd76yjclqr` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`),
  CONSTRAINT `FKgik7ruym8r1n4xngrclc6kiih` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction`
--

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction` DISABLE KEYS */;
INSERT INTO `transaction` VALUES (1,'REALICÉ',150,'OUT','2024-02-07 00:00:00.000000','tyututyu',NULL,0,'NOT_APPLICABLE','EXPENSE',1,1,NULL,1,NULL,2),(2,'RECIBÍ',100,'IN','2024-02-09 06:07:00.000000','v-server] Server started: Hot Module Replacement disabled, Live Reloading enabled, Progress disabled, Overlay enabled',NULL,0,'NOT_APPLICABLE','INCOME',1,24,NULL,5,NULL,NULL),(3,'RECIBÍ',400,'IN','2024-02-10 05:00:00.000000','receivedOrderClosePopUp receivedOrderClosePopUpreceivedOrderClosePopUp',NULL,400,'PENDING','LOAN',1,NULL,1,1,NULL,NULL),(4,'REALICÉ',15,'OUT','2024-02-11 01:12:38.142000','we rwe r',NULL,0,'NOT_APPLICABLE','EXPENSE',1,2,NULL,1,NULL,NULL),(5,'RECIBÍ',15,'IN','2024-02-13 00:00:00.000000','',NULL,0,'NOT_APPLICABLE','INCOME',1,2,NULL,15,NULL,NULL),(6,'RECIBÍ',900,'IN','2024-02-12 00:00:00.000000','kyukyukyuk',NULL,0,'NOT_APPLICABLE','INCOME',1,4,NULL,16,NULL,NULL),(7,'RECIBÍ',80,'IN','2024-02-09 00:00:00.000000','uyiy',NULL,0,'NOT_APPLICABLE','INCOME',1,1,NULL,17,NULL,NULL),(8,'REALICÉ',100,'OUT','2023-12-13 22:56:16.000000','',NULL,0,'NOT_APPLICABLE','EXPENSE',1,6,NULL,1,NULL,NULL),(9,'REALICÉ',200,'OUT','2024-02-13 23:22:01.000000','Test',NULL,0,'NOT_APPLICABLE','EXPENSE',1,6,NULL,1,NULL,NULL),(10,'RECIBÍ',420,'IN','2024-01-04 23:29:21.000000','qweq we qwe ',NULL,0,'NOT_APPLICABLE','INCOME',1,3,NULL,5,NULL,NULL),(11,'REALICÉ',30,'OUT','2024-02-07 23:32:42.000000','111111111111111',NULL,0,'NOT_APPLICABLE','EXPENSE',1,NULL,NULL,1,NULL,NULL),(12,'RECIBÍ',50,'IN','2024-02-15 15:18:16.000000','test',NULL,0,'NOT_APPLICABLE','INCOME',1,25,NULL,1,NULL,NULL),(13,'REALICÉ',5,'OUT','2024-02-15 16:24:30.000000','test 2',NULL,0,'NOT_APPLICABLE','EXPENSE',1,NULL,NULL,1,NULL,NULL),(14,'RECIBÍ',20,'IN','2024-02-15 16:38:11.000000','qweqweqw eqwe q',NULL,0,'NOT_APPLICABLE','INCOME',1,23,NULL,1,NULL,NULL),(15,'REALICÉ',100,'OUT','2024-02-15 16:38:11.000000','',NULL,0,'NOT_APPLICABLE','EXPENSE',1,NULL,NULL,1,NULL,NULL),(16,'REALICÉ',70,'OUT','2024-02-15 17:15:08.000000','',NULL,0,'NOT_APPLICABLE','EXPENSE',1,NULL,NULL,5,NULL,NULL),(17,'RECIBÍ',150,'IN','2024-02-15 23:29:25.000000','prestamo para pago de paseo con los panas a can cun',NULL,150,'PENDING','LOAN',1,26,2,1,NULL,2),(18,'REALICÉ',10,'OUT','2024-02-15 23:39:40.000000','qweq ',NULL,0,'NOT_APPLICABLE','EXPENSE',1,NULL,NULL,5,NULL,NULL),(19,'REALICÉ',150,'NOT_APPLICABLE','2024-02-16 21:24:49.000000','retiro para pago mensual de alquiler',NULL,0,'NOT_APPLICABLE','TRANSFERENCE',1,NULL,NULL,2,1,NULL),(20,'REALICÉ',130,'OUT','2024-02-16 21:28:18.000000','pago de alquiler',NULL,0,'NOT_APPLICABLE','EXPENSE',1,2,NULL,1,NULL,1),(21,'REALICÉ',8,'OUT','2024-02-11 01:12:38.142000','we rwe r',NULL,0,'NOT_APPLICABLE','EXPENSE',1,1,NULL,1,NULL,NULL),(22,'REALICÉ',8,'OUT','2024-02-11 01:12:38.142000','we rwe r',NULL,0,'NOT_APPLICABLE','EXPENSE',1,1,NULL,1,NULL,NULL),(23,'REALICÉ',5,'OUT','2024-02-11 01:12:38.142000','we rwe r',NULL,0,'NOT_APPLICABLE','EXPENSE',1,1,NULL,1,NULL,NULL),(24,'REALICÉ',5,'OUT','2024-02-11 01:12:38.142000','we rwe r',NULL,0,'NOT_APPLICABLE','EXPENSE',1,NULL,NULL,1,NULL,NULL),(25,'REALICÉ',10,'OUT','2024-02-21 02:46:34.000000','rturt urtu rtu rtu rt ur',NULL,0,'NOT_APPLICABLE','EXPENSE',1,1,NULL,13,NULL,NULL),(26,'RECIBÍ',20,'IN','2024-02-21 02:47:40.000000','uio uiot uiot uiyo yuio',NULL,20,'PENDING','LOAN',1,4,1,14,NULL,1);
/*!40000 ALTER TABLE `transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `authorities` varbinary(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `is_not_locked` bit(1) NOT NULL,
  `join_date` datetime(6) DEFAULT NULL,
  `last_login_date` datetime(6) DEFAULT NULL,
  `last_login_date_display` datetime(6) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKf9dvvibvpfsldnu8wh3enop4i` (`username`,`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,_binary '�\�\0ur\0[Ljava.lang.String;�\�V\�\�{G\0\0xp\0\0\0t\0	user:readt\0user:createt\0user:updatet\0user:delete','peru','everjrosalesp@gmail.com','ever junior',_binary '',_binary '','2024-02-09 19:17:02.853892','2024-02-21 02:31:47.015517','2024-02-20 18:08:16.761596','rosales peña','$2a$10$aXEM/Ny2CPV0D7ij8VU77OdxRUTNSpCmBm6IEwmdlUU2/U2XpK0Ci','ROLE_ADMIN','erosales');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-02-21  2:50:15
