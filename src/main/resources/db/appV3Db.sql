-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: trackerfinancedbSecurity
-- ------------------------------------------------------
-- Server version	8.2.0

--
-- Table structure for table `account`
--
create database trackerfinanceSecuritydbV3;
USE trackerfinanceSecuritydbV3;
DROP TABLE IF EXISTS `account`;

CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `current_balance` double NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `account`
--

INSERT INTO `account` VALUES (1,150,'EFECTIVO',1),(2,150,'BBVA',1),(3,150,'BCP',1),(4,5000,'SAGA FALABELLA',1),(5,70,'PREXPE',1);

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` VALUES (1,'PRESTAMO',1),(2,'ALQUILER',1),(3,'PENSION',1),(4,'MERCADO',1),(5,'NETFLIX',1),(6,'CRUNCHYROLL',1),(7,'PRIME VIDEO',1),(8,'SPOTIFY',1),(9,'GUSTITOS',1),(10,'MOVILIDAD',1),(11,'CABLE VISION STA ANITA',1),(12,'FAMILIA',1),(13,'SALIDA AMIGOS',1),(14,'SALIDA FAMILIA',1),(15,'SERVICIOS AGUA',1),(16,'SERVICIOS LUZ',1),(17,'SERVICIOS GAS',1),(18,'SERVICIOS GAS BALON',1),(19,'MOVIL EVER',1);


--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;

CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `member`
--

INSERT INTO `member` VALUES (1,'cinthia.aries.154@gmail.com','cinthia eduardo',1);

--
-- Table structure for table `payment_method`
--

DROP TABLE IF EXISTS `payment_method`;

CREATE TABLE `payment_method` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK63xr2pgmapw8eecpfwmfoas7l` (`account_id`),
  CONSTRAINT `FK63xr2pgmapw8eecpfwmfoas7l` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `payment_method`
--

INSERT INTO `payment_method` VALUES (1,'EFECTIVO',1),(2,'BCP CARD',3),(3,'BCP APP',3),(4,'YAPE',3),(5,'PLIN',2),(6,'BBVA CARD',2),(7,'BBVA APP',2),(8,'SAGA APP',4),(9,'SAGA CARD',4),(10,'PREXPE APP',5),(11,'PREXPE CARD',5);

--
-- Table structure for table `segment`
--

DROP TABLE IF EXISTS `segment`;

CREATE TABLE `segment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `segment`
--

INSERT INTO `segment` VALUES (1,'FIJO',1),(2,'VARIABLE',1);

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `transaction`

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
