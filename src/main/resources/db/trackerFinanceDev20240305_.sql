CREATE DATABASE  IF NOT EXISTS `prodtrackerfinance` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `prodtrackerfinance`;

-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;

CREATE TABLE `accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `begin_balance` decimal(38,2) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `current_balance` decimal(38,2) DEFAULT NULL,
  `fixed_parameter` bit(1) NOT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `card_type_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi46u16m1edy8vfq4s6gf1bj35` (`card_type_id`),
  CONSTRAINT `FKi46u16m1edy8vfq4s6gf1bj35` FOREIGN KEY (`card_type_id`) REFERENCES `card_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` VALUES (1,_binary '',1.00,NULL,41.40,_binary '\0',NULL,'EFECTIVO',1,2),(2,_binary '',NULL,'',960.77,_binary '\0','','Bcp',1,3),(3,_binary '',NULL,'',132.51,_binary '\0','','Bbva',1,3),(4,_binary '',NULL,'',618.82,_binary '\0','','Warda',1,4),(5,_binary '',NULL,'',3666.37,_binary '\0','','Cuenta Padre',1,3);

--
-- Table structure for table `budget`
--

CREATE TABLE `budget` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `date_begin` datetime(6) DEFAULT NULL,
  `date_end` datetime(6) DEFAULT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `has_automatic_regeneration` bit(1) NOT NULL,
  `limit_amount` decimal(38,2) DEFAULT NULL,
  `status_open` bit(1) NOT NULL,
  `used_amount` decimal(38,2) DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `budget`
--
--
-- Table structure for table `budget_sub_categories`
--

DROP TABLE IF EXISTS `budget_sub_categories`;

CREATE TABLE `budget_sub_categories` (
  `budget_id` bigint NOT NULL,
  `sub_categories_id` bigint NOT NULL,
  UNIQUE KEY `UK_fvojhvrjjcfmf35ulmhbexf6g` (`sub_categories_id`),
  KEY `FKh6hwiscyajo3fjnnhx2c36rf` (`budget_id`),
  CONSTRAINT `FKh6hwiscyajo3fjnnhx2c36rf` FOREIGN KEY (`budget_id`) REFERENCES `budget` (`id`),
  CONSTRAINT `FKki2gm6qem3jpusaep1m78t9cd` FOREIGN KEY (`sub_categories_id`) REFERENCES `sub_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
--
-- Dumping data for table `budget_sub_categories`
--
--
-- Table structure for table `budget_transactions`
--

DROP TABLE IF EXISTS `budget_transactions`;

CREATE TABLE `budget_transactions` (
  `budget_id` bigint NOT NULL,
  `transactions_id` bigint NOT NULL,
  UNIQUE KEY `UK_9ooliguroep4wq0ob1hxsc9y7` (`transactions_id`),
  KEY `FK5539qkawgbkm9hoo1m39fyia5` (`budget_id`),
  CONSTRAINT `FK5539qkawgbkm9hoo1m39fyia5` FOREIGN KEY (`budget_id`) REFERENCES `budget` (`id`),
  CONSTRAINT `FK701af4r6ouwnd0iik9qrucpj7` FOREIGN KEY (`transactions_id`) REFERENCES `transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `budget_transactions`
--

-- Table structure for table `card_type`
--

DROP TABLE IF EXISTS `card_type`;

CREATE TABLE `card_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `fixed_parameter` bit(1) NOT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `card_type`
--

INSERT INTO `card_type` VALUES (1,NULL,_binary '',NULL,'CREDITO',1),(2,'',_binary '\0','','EFECTIVO',1),(3,'',_binary '\0','','DÉBITO',1),(4,'',_binary '\0','','Ahorro',1);

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` VALUES (1,'Fijos',1),(2,'Mascotas',1),(3,'suscripciones',1),(4,'Salud',1);


--
-- Table structure for table `counterpart`
--

DROP TABLE IF EXISTS `counterpart`;

CREATE TABLE `counterpart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `counterpart`
--

INSERT INTO `counterpart` VALUES (1,'','Suegra',1);
--
-- Table structure for table `payment_method`
--

DROP TABLE IF EXISTS `payment_method`;

CREATE TABLE `payment_method` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `color` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `used` bit(1) NOT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1mqkcijfuokwqygybafyi3wqk` (`account_id`),
  CONSTRAINT `FK1mqkcijfuokwqygybafyi3wqk` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `payment_method`
--

INSERT INTO `payment_method` VALUES (1,_binary '','','','Yape',_binary '',1,2),(2,_binary '','','','AppBcp',_binary '',1,2),(3,_binary '','','','Plin',_binary '',1,3),(4,_binary '','','','OnlineBcp',_binary '',1,2),(5,_binary '','','','TarjetaBcp',_binary '',1,2),(6,_binary '','','','AppBbva',_binary '',1,3);

--
-- Table structure for table `recurring`
--

DROP TABLE IF EXISTS `recurring`;

CREATE TABLE `recurring` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `day_month` int NOT NULL,
  `item_date_selected_per_period` varbinary(255) DEFAULT NULL,
  `next_closest_payment_date` datetime(6) DEFAULT NULL,
  `next_payment_date` datetime(6) DEFAULT NULL,
  `number_of_times` int NOT NULL,
  `period` enum('DAY','WEEK','MONTH','YEAR') DEFAULT NULL,
  `status_is_payed` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `recurring`
--

--
-- Table structure for table `sub_category`
--

DROP TABLE IF EXISTS `sub_category`;

CREATE TABLE `sub_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `color` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `used` bit(1) NOT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl65dyy5me2ypoyj8ou1hnt64e` (`category_id`),
  CONSTRAINT `FKl65dyy5me2ypoyj8ou1hnt64e` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
--
-- Dumping data for table `sub_category`
--

INSERT INTO `sub_category` VALUES (1,_binary '','','','Alquiler',_binary '\0',1,1),(2,_binary '','','','Agua',_binary '\0',1,1),(3,_binary '','','','Luz',_binary '\0',1,1),(4,_binary '','','','Gas',_binary '\0',1,1),(5,_binary '','','','Comida Pepe',_binary '\0',1,2),(6,_binary '','','','Arena pepe',_binary '\0',1,2),(7,_binary '','','','Juegos pepes',_binary '\0',1,2),(8,_binary '','','','Gustitos',_binary '\0',1,NULL),(9,_binary '','','','Cable Visión Sta Anita',_binary '\0',1,1),(10,_binary '','','','Internet FibraMax',_binary '\0',1,1),(11,_binary '','','','Suscripción BetaShop',_binary '\0',1,3),(12,_binary '','','','Spotify',_binary '\0',1,3),(13,_binary '','','','Pareja',_binary '\0',1,NULL),(14,_binary '','','','curso',_binary '\0',1,NULL),(15,_binary '','','','Google Drive',_binary '\0',1,3),(16,_binary '','','','compras diarias',_binary '\0',1,NULL),(17,_binary '','','','Familia',_binary '\0',1,NULL),(18,_binary '','','','Recreacion',_binary '\0',1,NULL),(19,_binary '','','','Pensión',_binary '\0',1,1),(20,_binary '','','','desconocido',_binary '\0',1,NULL),(21,_binary '','','','mercado',_binary '\0',1,NULL),(22,_binary '','','','Youtube',_binary '\0',1,3),(23,_binary '','','','Jarabe',_binary '\0',1,4);

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;

CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
--
-- Dumping data for table `tag`
--

INSERT INTO `tag` VALUES (1,'Piscina, Paraíso del Sur',1),(2,'Netflix, Prime Video, Crunchyrool, Max',1),(3,'carnes, verduras, abarrotes, lacteos',1),(4,'Lacetos- queso',1),(5,'huevos',1),(6,'visita madre',1),(7,'visita suegra',1),(8,'Mi cumpleaños',1);

--
-- Table structure for table `tag_transaction`
--

DROP TABLE IF EXISTS `tag_transaction`;

CREATE TABLE `tag_transaction` (
  `fk_transaction` bigint NOT NULL,
  `fk_tag` bigint NOT NULL,
  KEY `FKldx3hai83pf92ryhk2mtn7ofu` (`fk_tag`),
  KEY `FKhkq827eryg5ot8t0y63dhva6x` (`fk_transaction`),
  CONSTRAINT `FKhkq827eryg5ot8t0y63dhva6x` FOREIGN KEY (`fk_transaction`) REFERENCES `transaction` (`id`),
  CONSTRAINT `FKldx3hai83pf92ryhk2mtn7ofu` FOREIGN KEY (`fk_tag`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `tag_transaction`
--

INSERT INTO `tag_transaction` VALUES (18,1),(19,1),(20,1),(21,1),(27,2),(35,4),(36,5),(33,3),(38,6),(38,7),(38,8);

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;

CREATE TABLE `transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('RECIBÍ','REALICÉ','NOT_APPLICABLE') DEFAULT NULL,
  `amount` decimal(38,2) DEFAULT NULL,
  `block` enum('IN','OUT','NOT_APPLICABLE') DEFAULT NULL,
  `create_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `payment_date` datetime(6) DEFAULT NULL,
  `remaining` decimal(38,2) DEFAULT NULL,
  `status` enum('PENDING','PAYED','NOT_APPLICABLE') DEFAULT NULL,
  `type` enum('LOAN','PAYMENT','EXPENSE','INCOME','TRANSFERENCE') DEFAULT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  `account_destiny_id` bigint DEFAULT NULL,
  `counterpart_id` bigint DEFAULT NULL,
  `payment_method_id` bigint DEFAULT NULL,
  `payment_method_destiny_id` bigint DEFAULT NULL,
  `recurring_id` bigint DEFAULT NULL,
  `responsable_user` bigint NOT NULL,
  `sub_category_id` bigint DEFAULT NULL,
  `transaction_loan_assoc_to_pay_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gdcjslpfoccler8wwn6rfse8v` (`recurring_id`),
  KEY `FKaqehs9hoj644s1sab3ge0wrs3` (`account_id`),
  KEY `FKb26e55j7d4tfqaa2u4x9l1rg7` (`account_destiny_id`),
  KEY `FK8sqmeglbb4dwpajlvd1wudy5e` (`counterpart_id`),
  KEY `FK3iabppaif3d2a8jjotnly31j2` (`payment_method_id`),
  KEY `FK9nqkrnibs32837dmot6o5x8eg` (`payment_method_destiny_id`),
  KEY `FK96qafn6jobltp2gy088la6o3g` (`responsable_user`),
  KEY `FKpvm74p9el5nkjpntui02tqsj9` (`sub_category_id`),
  KEY `FK7m9m2rxp9040w1k90p80py4ax` (`transaction_loan_assoc_to_pay_id`),
  CONSTRAINT `FK3iabppaif3d2a8jjotnly31j2` FOREIGN KEY (`payment_method_id`) REFERENCES `payment_method` (`id`),
  CONSTRAINT `FK53wev7okpjjdux5u38td5l9tu` FOREIGN KEY (`recurring_id`) REFERENCES `recurring` (`id`),
  CONSTRAINT `FK7m9m2rxp9040w1k90p80py4ax` FOREIGN KEY (`transaction_loan_assoc_to_pay_id`) REFERENCES `transaction` (`id`),
  CONSTRAINT `FK8sqmeglbb4dwpajlvd1wudy5e` FOREIGN KEY (`counterpart_id`) REFERENCES `counterpart` (`id`),
  CONSTRAINT `FK96qafn6jobltp2gy088la6o3g` FOREIGN KEY (`responsable_user`) REFERENCES `user` (`id`),
  CONSTRAINT `FK9nqkrnibs32837dmot6o5x8eg` FOREIGN KEY (`payment_method_destiny_id`) REFERENCES `payment_method` (`id`),
  CONSTRAINT `FKaqehs9hoj644s1sab3ge0wrs3` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FKb26e55j7d4tfqaa2u4x9l1rg7` FOREIGN KEY (`account_destiny_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FKpvm74p9el5nkjpntui02tqsj9` FOREIGN KEY (`sub_category_id`) REFERENCES `sub_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `transaction`
--

INSERT INTO `transaction` VALUES (1,'RECIBÍ',30.00,'IN','2024-02-15 10:30:09.000000','colaboración de cinthia por 14 de febrero',NULL,0.00,'NOT_APPLICABLE','INCOME',1,2,NULL,NULL,1,NULL,NULL,1,13,NULL),(2,'NOT_APPLICABLE',14.48,'NOT_APPLICABLE','2024-02-15 12:33:45.000000','Retiro automático a Warda',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,2,4,NULL,NULL,NULL,NULL,1,NULL,NULL),(3,'REALICÉ',42.00,'OUT','2024-02-18 21:34:38.000000','Pollo a la brasa 1/2 pollo +1/4 - Pollería Don Rojas',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,8,NULL),(4,'REALICÉ',12.75,'OUT','2024-02-18 19:53:36.000000','Compra de curso de manejo de SVG en doméstica',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,4,NULL,NULL,1,14,NULL),(5,'REALICÉ',110.61,'OUT','2024-02-18 10:59:02.000000','Google storage, suscripción anual',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,5,NULL,NULL,1,15,NULL),(6,'REALICÉ',79.90,'OUT','2024-02-19 12:02:02.000000','Pago internet-Fibramax',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,10,NULL),(7,'REALICÉ',50.00,'OUT','2024-02-19 13:02:55.000000','Mensualidad de Cable visión- Sta Anita',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,2,NULL,NULL,1,9,NULL),(8,'REALICÉ',14.00,'OUT','2024-02-21 14:09:30.000000','Gaseosa coca cola 1lt[6 soles + 2 por botella], 1 frio rico [5] y 2 sanguchitos.[3]',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,8,NULL),(9,'REALICÉ',6.00,'OUT','2024-02-23 16:14:17.000000','Gaseosa 1lt + aceite',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,16,NULL),(10,'REALICÉ',20.00,'OUT','2024-02-24 16:15:28.000000','compras rápidas mercado,  palta, cebolla, tomtae y papa',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,16,NULL),(11,'REALICÉ',42.00,'OUT','2024-02-25 17:16:23.000000','pollo a la brassa Don Juan [rico :) ]',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,8,NULL),(12,'REALICÉ',37.80,'OUT','2024-02-26 14:20:26.000000','Visita a casa de suegra por Don Armando . [sandía, cerverza par ron pope, flores, y pasajes], almuerzo lo pagó cinthia.',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,17,NULL),(13,'NOT_APPLICABLE',40.00,'NOT_APPLICABLE','2024-02-27 14:26:38.000000','Plineo desde Yape para pago de Cafae',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,2,3,NULL,NULL,NULL,NULL,1,NULL,NULL),(14,'REALICÉ',38.00,'OUT','2024-02-27 15:29:34.000000','pago de cafae - suegra',NULL,38.00,'PENDING','LOAN',1,3,NULL,1,6,NULL,NULL,1,17,NULL),(15,'REALICÉ',20.00,'OUT','2024-02-28 20:30:41.000000','Popeyes',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,8,NULL),(16,'REALICÉ',65.00,'OUT','2024-02-27 21:31:42.000000','Arena para gato 4 sacos de 25Kg c/u',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,6,NULL),(17,'REALICÉ',4.60,'OUT','2024-03-01 13:33:13.000000','Gaseosa Sprite(3.5) y 1 palta (1.10)',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,16,NULL),(18,'REALICÉ',210.00,'OUT','2024-03-03 10:38:16.000000','Salida Familiar a Piscina Paraíso, entrada 70 c/u -  del Sur  ',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,5,NULL,NULL,1,18,NULL),(19,'REALICÉ',28.00,'OUT','2024-03-03 10:39:16.000000','Salida Familiar a Piscina Paraíso del Sur - Pasajes Uber',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,3,NULL,NULL,NULL,NULL,NULL,1,18,NULL),(20,'REALICÉ',70.00,'OUT','2024-03-03 12:40:10.000000','Salida Familiar a Piscina Paraíso del Sur - Ropa de Baño para los 3',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,18,NULL),(21,'REALICÉ',30.00,'OUT','2024-03-03 15:40:51.000000','Salida Familiar a Piscina Paraíso del Sur - Silla Playa',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,18,NULL),(22,'NOT_APPLICABLE',333.63,'NOT_APPLICABLE','2024-03-03 18:46:55.000000','Recarga externa de saldo',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,5,2,NULL,NULL,NULL,NULL,1,NULL,NULL),(23,'REALICÉ',400.00,'OUT','2024-03-03 19:50:02.000000','Pensión de la madre - Yapero a través de Eli (hermana)',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,19,NULL),(24,'REALICÉ',12.00,'OUT','2024-03-04 11:50:53.000000','Aceite, yucas, zanahoria y huevos',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,16,NULL),(25,'NOT_APPLICABLE',16.68,'NOT_APPLICABLE','2024-03-04 16:26:38.000000',' Retiro automático a Warda',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,2,4,NULL,NULL,NULL,NULL,1,NULL,NULL),(26,'RECIBÍ',210.00,'IN','2024-03-04 20:29:32.000000','Yapeo de entradas a piscina Paraiso del sur, de parte mi hermana, se pasó.',NULL,0.00,'NOT_APPLICABLE','INCOME',1,2,NULL,NULL,1,NULL,NULL,1,17,NULL),(27,'REALICÉ',21.00,'OUT','2024-03-02 12:15:30.000000','Servicios betashop - suscripcion mensual',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,11,NULL),(28,'REALICÉ',10.80,'OUT','2024-02-26 16:43:12.000000','Desconocido, pero se encuentra en Yape',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,20,NULL),(29,'NOT_APPLICABLE',1000.00,'NOT_APPLICABLE','2024-03-05 01:00:08.000000','recarga externa a bcp',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,5,2,NULL,NULL,NULL,NULL,1,NULL,NULL),(30,'NOT_APPLICABLE',50.00,'NOT_APPLICABLE','2024-03-05 01:21:04.000000','plineo desde yape por bcp para que supere bbva los 100, para que no cobre comision',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,2,3,NULL,NULL,NULL,NULL,1,NULL,NULL),(31,'NOT_APPLICABLE',10.50,'NOT_APPLICABLE','2024-03-05 10:26:50.000000','retiro automático a warda',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,2,4,NULL,NULL,NULL,NULL,1,NULL,NULL),(32,'NOT_APPLICABLE',200.00,'NOT_APPLICABLE','2024-03-05 11:27:57.000000','Retiro para compras mercado de 200 de Bcp a Efectivo',NULL,0.00,'NOT_APPLICABLE','TRANSFERENCE',1,2,1,NULL,NULL,NULL,NULL,1,NULL,NULL),(33,'REALICÉ',200.40,'OUT','2024-03-05 11:36:45.000000','compras mercado para la semana',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,1,NULL,NULL,NULL,NULL,NULL,1,21,NULL),(34,'REALICÉ',49.40,'OUT','2024-03-05 11:37:58.000000','compras mercado para la semana - ABARROTES',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,21,NULL),(35,'REALICÉ',22.10,'OUT','2024-03-05 11:39:05.000000','compras mercado para la semana - Queso Parea',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,21,NULL),(36,'REALICÉ',15.50,'OUT','2024-03-05 11:40:13.000000','compras mercado para la semana - Huevos',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,1,NULL,NULL,1,21,NULL),(37,'REALICÉ',35.36,'OUT','2024-03-02 12:17:47.000000','suscripción youtube premium',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,4,NULL,NULL,1,22,NULL),(38,'REALICÉ',31.00,'OUT','2024-03-05 21:07:48.000000','jarabe Acit tip para los gases - para mi madre, de visita por mi cumple, y mi suegra tbn :D',NULL,0.00,'NOT_APPLICABLE','EXPENSE',1,2,NULL,NULL,5,NULL,NULL,1,23,NULL);

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
  `user_parent_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKf9dvvibvpfsldnu8wh3enop4i` (`username`,`email`),
  KEY `FK7njgbg32goka7y6kqpwo6pgm2` (`user_parent_id`),
  CONSTRAINT `FK7njgbg32goka7y6kqpwo6pgm2` FOREIGN KEY (`user_parent_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` VALUES (1,_binary '�\�\0ur\0[Ljava.lang.String;�\�V\�\�{G\0\0xp\0\0\0t\0all:readt\0\nall:createt\0\nall:updatet\0\nall:delete',NULL,'admin1@gmail.com','ever rosales',_binary '',_binary '','2024-03-04 16:52:13.050640','2024-03-05 23:04:44.726796','2024-03-04 21:06:32.999372',NULL,'$2a$10$Cd0Tp7UI.8qRbLzceAQNAO7PItJIYM/ZTaFRDCklK0kMfHyF3jwwq','ROLE_SUPER_ADMIN','ejrosalesp',NULL);
--
-- Table structure for table `user_workspace`
--

DROP TABLE IF EXISTS `user_workspace`;

CREATE TABLE `user_workspace` (
  `workspace_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  KEY `FKm08gh5dm1n84gx081ryd6wh7y` (`user_id`),
  KEY `FKgbyqvx4cb94thslkrd3o41tth` (`workspace_id`),
  CONSTRAINT `FKgbyqvx4cb94thslkrd3o41tth` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`id`),
  CONSTRAINT `FKm08gh5dm1n84gx081ryd6wh7y` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
--
-- Dumping data for table `user_workspace`
--

INSERT INTO `user_workspace` VALUES (1,1);

--
-- Table structure for table `workspace`
--

DROP TABLE IF EXISTS `workspace`;

CREATE TABLE `workspace` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk7dgp9sb1paeoxv6iudh1snt5` (`owner_id`),
  CONSTRAINT `FKk7dgp9sb1paeoxv6iudh1snt5` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `workspace`
--

INSERT INTO `workspace` VALUES (1,_binary '','My Workspace',1);


-- Dump completed on 2024-03-05 23:17:57
