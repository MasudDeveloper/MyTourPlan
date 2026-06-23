<?php
// db_patch.php
header("Content-Type: application/json");
require_once 'config.php';

try {
    // 1. Check and add 'profile_pic' to 'users' table
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'profile_pic'");
    $columnExists = $stmt->fetch();

    if (!$columnExists) {
        $conn->exec("ALTER TABLE users ADD COLUMN profile_pic VARCHAR(255) DEFAULT NULL AFTER phone");
    }

    // 2. Create 'itinerary' table if it doesn't exist
    $conn->exec("CREATE TABLE IF NOT EXISTS `itinerary` (
        `id` INT AUTO_INCREMENT PRIMARY KEY,
        `trip_id` INT NOT NULL,
        `day` INT NOT NULL,
        `time` VARCHAR(50) NOT NULL,
        `activity` VARCHAR(255) NOT NULL,
        `location` VARCHAR(150),
        FOREIGN KEY (`trip_id`) REFERENCES `trips`(`id`) ON DELETE CASCADE
    )");

    // 3. Create 'trip_notes' table if it doesn't exist
    $conn->exec("CREATE TABLE IF NOT EXISTS `trip_notes` (
        `id` INT AUTO_INCREMENT PRIMARY KEY,
        `trip_id` INT NOT NULL,
        `title` VARCHAR(255) NOT NULL,
        `content` TEXT DEFAULT NULL,
        `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (`trip_id`) REFERENCES `trips`(`id`) ON DELETE CASCADE
    )");

    // 4. Create 'trip_checklists' table if it doesn't exist
    $conn->exec("CREATE TABLE IF NOT EXISTS `trip_checklists` (
        `id` INT AUTO_INCREMENT PRIMARY KEY,
        `trip_id` INT NOT NULL,
        `title` VARCHAR(255) NOT NULL,
        `is_checked` INT DEFAULT 0,
        `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (`trip_id`) REFERENCES `trips`(`id`) ON DELETE CASCADE
    )");

    echo json_encode([
        "success" => true,
        "message" => "Database patched successfully. All tables (itinerary, trip_notes, trip_checklists) checked and created, users table altered if necessary."
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => "Failed to patch database: " . $e->getMessage()
    ]);
}
?>
