-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: bookstore
-- ------------------------------------------------------
-- Server version	8.0.40

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

-- DROP DATABASE and RECREATE (Clean Slate Approach)
DROP DATABASE IF EXISTS bookstore;
CREATE DATABASE bookstore;
USE bookstore;

--
-- Table structure for table `books`
--

CREATE TABLE `books` (
  `bookId` int NOT NULL AUTO_INCREMENT,
  `type` varchar(100) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `publisher` varchar(255) DEFAULT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`bookId`)
) ENGINE=InnoDB AUTO_INCREMENT=100038 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `books`
--

INSERT INTO `books` VALUES 
(100000,'Fiction','To Kill a Mockingbird',18.99,'J.B. Lippincott & Co.',8),
(100001,'Fiction','1984',14.99,'Secker & Warburg',0),
(100002,'Fiction','Pride and Prejudice',12.99,'T. Egerton',50),
(100003,'Fiction','The Great Gatsby',10.99,'Charles Scribner\'s Sons',197),
(100004,'Fiction','Moby-Dick',16.5,'Harper & Brothers',11),
(100005,'Science-Fiction','Dune',15.5,'Chilton Books',72),
(100006,'Science-Fiction','Neuromancer',13.99,'Ace',10),
(100007,'Science-Fiction','Fahrenheit 451',22,'ddfasd',12),
(100008,'Science-Fiction','The Left Hand of Darkness',12.5,'Ace',5),
(100009,'Science-Fiction','Ender\'s Game',14.99,'Tor Books',9),
(100010,'Fantasy','Harry Potter and the Sorcerer\'s Stone',20,'Bloomsbury',15),
(100011,'Fantasy','The Hobbit',14.99,'George Allen & Unwin',20),
(100012,'Fantasy','The Name of the Wind',17.99,'DAW Books',12),
(100013,'Fantasy','American Gods',16.5,'William Morrow',9),
(100014,'Fantasy','Mistborn: The Final Empire',15.5,'Tor Books',7),
(100015,'Non-Fiction','Sapiens: A Brief History of Humankind',22.99,'Harvill Secker',12),
(100016,'Non-Fiction','Educated',19.99,'Random House',10),
(100017,'Non-Fiction','Becoming',24.99,'Crown',14),
(100018,'Non-Fiction','The Immortal Life of Henrietta Lacks',16.99,'Crown',100),
(100019,'Fiction','Thinking, Fast and Slow',18.5,'Farrar, Straus and Giroux',111),
(100020,'Biography','The Diary of a Young Girl',10.99,'Contact Publishing',5),
(100021,'Biography','Steve Jobs',25,'Simon & Schuster',10),
(100022,'Biography','Long Walk to Freedom',18.99,'Little, Brown and Company',6),
(100023,'Biography','Becoming',24.99,'Crown',8),
(100024,'Biography','The Wright Brothers',14.99,'Simon & Schuster',4),
(100026,'Manga','One Piece',10.99,'Shueisha',1000),
(100027,'Manga','Attack on Titan',11.5,'Kodansha',18),
(100028,'Manga','My Hero Academia',8.99,'Shueisha',222);

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `username` varchar(50) NOT NULL,
  `firstName` varchar(20) NOT NULL,
  `lastName` varchar(20) NOT NULL,
  `phoneNumber` varchar(15) DEFAULT NULL,
  `password` varchar(50) NOT NULL,
  `role` varchar(20) DEFAULT 'customer',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` VALUES
('admin','Harry','Vu','1234567899','adminpass','admin'),
('alice','Alice','Smith','1234567890','password123','customer'),
('bob','Bob','Johnson','2345678901','bobsecure','customer'),
('nghia123','Harry','Vu','01234224782','123456789','customer'),
('khoa','Khoa Le','Tran','44115487211','00000000','customer'),
('khoa123','Khoa','Nguyen','087159158','0123456789','customer'),
('phamthithi','Thi','Thi','0123456789','0123456789','customer');

--
-- Table structure for table `orders` (WITH ALL REQUIRED COLUMNS)
--

CREATE TABLE `orders` (
  `orderId` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `orderDate` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) NOT NULL DEFAULT 'Pending',
  `totalAmount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `totalItems` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`orderId`),
  KEY `idx_orders_username` (`username`),
  KEY `idx_orders_date` (`orderDate`),
  CONSTRAINT `fk_orders_username` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=200018 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `orderdetails` (WITH ALL REQUIRED COLUMNS)
--

CREATE TABLE `orderdetails` (
  `orderDetailId` int NOT NULL AUTO_INCREMENT,
  `orderId` int NOT NULL,
  `bookId` int NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `price` double NOT NULL,
  `subtotal` DECIMAL(10,2) GENERATED ALWAYS AS (quantity * price) STORED,
  PRIMARY KEY (`orderDetailId`),
  UNIQUE KEY `unique_order_book` (`orderId`,`bookId`),
  KEY `idx_orderdetails_orderId` (`orderId`),
  KEY `idx_orderdetails_bookId` (`bookId`),
  CONSTRAINT `fk_orderdetails_orderId` FOREIGN KEY (`orderId`) REFERENCES `orders` (`orderId`) ON DELETE CASCADE,
  CONSTRAINT `fk_orderdetails_bookId` FOREIGN KEY (`bookId`) REFERENCES `books` (`bookId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `shoppingcart`
--

CREATE TABLE `shoppingcart` (
  `username` varchar(50) NOT NULL,
  `bookId` int NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `price` double NOT NULL,
  `dateAdded` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`username`,`bookId`),
  KEY `idx_cart_bookId` (`bookId`),
  CONSTRAINT `fk_cart_username` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_bookId` FOREIGN KEY (`bookId`) REFERENCES `books` (`bookId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Database creation completed successfully!
