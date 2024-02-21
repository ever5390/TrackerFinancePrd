CREATE DATABASE  IF NOT EXISTS `trackerfinancedb`;
USE `trackerfinancedb`;
-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: trackerfinancedb
-- ------------------------------------------------------
-- Server version	8.2.0

-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;

CREATE TABLE `account` (
  `current_balance` double NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `account`
--

INSERT INTO `account` (`current_balance`, `id`, `name`) VALUES (1500,1,'BCP'),(100,2,'BBVA'),(4000,3,'SAGA'),(4000,4,'CENCOSUD'),(50,5,'PREXPE');

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` (`id`, `name`) VALUES (1,'GUSTITOS'),(2,'FAMILIA'),(3,'AMIGOS'),(4,'SERVICIO AGUA'),(5,'SERVICIO LUZ'),(6,'ALQUILER'),(7,'PENSIÓN'),(8,'COMPRAS MERCADO'),(9,'NETLFLIX'),(10,'CABLE VISION STA ANITA'),(11,'MOVIL EVER'),(12,'MOVIL CINTHIA');

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;

CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `member`
--

INSERT INTO `member` (`id`, `email`, `name`) VALUES (1,'cinthia.aries.154@gmail.com','Cinthia');

--
-- Table structure for table `movement`
--

DROP TABLE IF EXISTS `movement`;

CREATE TABLE `movement` (
  `amount` double NOT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_transaction_assoc` bigint DEFAULT NULL,
  `account` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `segment` varchar(255) DEFAULT NULL,
  `member_name` varchar(255) DEFAULT NULL,
  `action` enum('RECIBÍ','REALICÉ','NOT_APPLICABLE') DEFAULT NULL,
  `block` enum('IN','OUT','NOT_APPLICABLE') DEFAULT NULL,
  `status` enum('PENDING','PAYED','NOT_APPLICABLE') DEFAULT NULL,
  `type` enum('LOAN','PAYMENT','EXPENSE','TRANSFERENCE','INCOME') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `payment_method`
--

DROP TABLE IF EXISTS `payment_method`;

CREATE TABLE `payment_method` (
  `account_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK63xr2pgmapw8eecpfwmfoas7l` (`account_id`),
  CONSTRAINT `FK63xr2pgmapw8eecpfwmfoas7l` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `payment_method`
--

INSERT INTO `payment_method` (`account_id`, `id`, `name`) VALUES (1,1,'YAPE'),(1,2,'BCP MOVIL'),(1,3,'BCP DEBIT CARD'),(2,4,'BBVA DEBIT CARD'),(2,5,'BBVA MOVIL'),(2,6,'PLIN'),(3,7,'SAGA CREDIT CARD'),(3,8,'SAGA MOVIL'),(4,9,'CENCOSUD MOVIL'),(4,10,'CENCOSUD CREDIT CARD'),(5,11,'PREXPE CARD'),(5,12,'PREXPE MOVIL');


--
-- Table structure for table `segment`
--

DROP TABLE IF EXISTS `segment`;

CREATE TABLE `segment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `segment`
--

INSERT INTO `segment` (`id`, `name`) VALUES (2,'NO FIJO'),(3,'FIJO');


--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;

CREATE TABLE `transaction` (
  `amount` double NOT NULL,
  `remaining` double NOT NULL,
  `category_id` bigint DEFAULT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_loan_assoc` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `payment_method_id` bigint DEFAULT NULL,
  `segment_id` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `action` enum('RECIBÍ','REALICÉ','NOT_APPLICABLE') DEFAULT NULL,
  `block` enum('IN','OUT','NOT_APPLICABLE') DEFAULT NULL,
  `status` enum('PENDING','PAYED','NOT_APPLICABLE') DEFAULT NULL,
  `type` enum('LOAN','PAYMENT','EXPENSE','TRANSFERENCE','INCOME') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgik7ruym8r1n4xngrclc6kiih` (`category_id`),
  KEY `FK8g210cmyp5dyikvxf9ek6ub30` (`member_id`),
  KEY `FK3iabppaif3d2a8jjotnly31j2` (`payment_method_id`),
  KEY `FKa7apm3ouijiwo82vd76yjclqr` (`segment_id`),
  CONSTRAINT `FK3iabppaif3d2a8jjotnly31j2` FOREIGN KEY (`payment_method_id`) REFERENCES `payment_method` (`id`),
  CONSTRAINT `FK8g210cmyp5dyikvxf9ek6ub30` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKa7apm3ouijiwo82vd76yjclqr` FOREIGN KEY (`segment_id`) REFERENCES `segment` (`id`),
  CONSTRAINT `FKgik7ruym8r1n4xngrclc6kiih` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `transference`
--

DROP TABLE IF EXISTS `transference`;

CREATE TABLE `transference` (
  `amount` double NOT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `payment_method_destiny_id` bigint DEFAULT NULL,
  `payment_method_origin_id` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKoiy1sjq5oxsw4jvcmriy5rba5` (`payment_method_destiny_id`),
  KEY `FK293ur6m62rc97940nkuygt6rk` (`payment_method_origin_id`),
  CONSTRAINT `FK293ur6m62rc97940nkuygt6rk` FOREIGN KEY (`payment_method_origin_id`) REFERENCES `payment_method` (`id`),
  CONSTRAINT `FKoiy1sjq5oxsw4jvcmriy5rba5` FOREIGN KEY (`payment_method_destiny_id`) REFERENCES `payment_method` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

