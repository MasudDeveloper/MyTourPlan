-- --------------------------------------------------------
-- Table structure for table `users`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `phone` VARCHAR(20),
  `password` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- --------------------------------------------------------
-- Table structure for table `trips`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `trips` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `from_location` VARCHAR(100),
  `destination` VARCHAR(100) NOT NULL,
  `image_uri` VARCHAR(255),
  `start_date` VARCHAR(50),
  `end_date` VARCHAR(50),
  `members_count` INT NOT NULL DEFAULT 1,
  `budget` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  `status` VARCHAR(50) DEFAULT 'Upcoming',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Table structure for table `expenses`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `expenses` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `trip_id` INT NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `amount` DECIMAL(10, 2) NOT NULL,
  `note` TEXT,
  `date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`trip_id`) REFERENCES `trips`(`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Table structure for table `itinerary`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `itinerary` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `trip_id` INT NOT NULL,
  `day` INT NOT NULL,
  `time` VARCHAR(50) NOT NULL,
  `activity` VARCHAR(255) NOT NULL,
  `location` VARCHAR(150),
  FOREIGN KEY (`trip_id`) REFERENCES `trips`(`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Table structure for table `hotels` (For Generation Plan)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotels` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `location` VARCHAR(100) NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `price_per_night` DECIMAL(10, 2) NOT NULL,
  `rating` DECIMAL(3, 1) NOT NULL,
  `type` ENUM('Cheap', 'Medium', 'Premium') NOT NULL
);

-- --------------------------------------------------------
-- Table structure for table `transport_options` (For Generation Plan)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `transport_options` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `from_location` VARCHAR(100) NOT NULL,
  `to_location` VARCHAR(100) NOT NULL,
  `type` VARCHAR(50) NOT NULL, -- Bus, Train, Flight
  `price` DECIMAL(10, 2) NOT NULL,
  `duration` VARCHAR(50) NOT NULL
);

-- Insert dummy data for locations
INSERT INTO `hotels` (`location`, `name`, `price_per_night`, `rating`, `type`) VALUES 
('Coxs Bazar', 'Sea Pearl', 15000, 4.8, 'Premium'),
('Coxs Bazar', 'Ocean Paradise', 8000, 4.5, 'Medium'),
('Coxs Bazar', 'Kollol Resort', 3000, 3.8, 'Cheap'),
('Sylhet', 'Grand Sultan', 20000, 4.9, 'Premium'),
('Sylhet', 'Rose View', 6000, 4.2, 'Medium'),
('Sylhet', 'Holy Gate', 2500, 3.5, 'Cheap')
ON DUPLICATE KEY UPDATE `name`=`name`;

INSERT INTO `transport_options` (`from_location`, `to_location`, `type`, `price`, `duration`) VALUES
('Dhaka', 'Coxs Bazar', 'Bus (AC)', 2000, '10 hours'),
('Dhaka', 'Coxs Bazar', 'Flight', 5000, '1 hour'),
('Dhaka', 'Sylhet', 'Train (AC)', 1200, '7 hours'),
('Dhaka', 'Sylhet', 'Bus (Non-AC)', 800, '6 hours')
ON DUPLICATE KEY UPDATE `type`=`type`;
