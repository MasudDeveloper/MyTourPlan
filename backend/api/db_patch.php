<?php
// db_patch.php
header("Content-Type: application/json");
require_once 'config.php';

try {
    // Check if profile_pic column exists
    $stmt = $conn->query("SHOW COLUMNS FROM users LIKE 'profile_pic'");
    $columnExists = $stmt->fetch();

    if (!$columnExists) {
        $conn->exec("ALTER TABLE users ADD COLUMN profile_pic VARCHAR(255) DEFAULT NULL AFTER phone");
        echo json_encode([
            "success" => true,
            "message" => "Database patched successfully. 'profile_pic' column has been added to the 'users' table."
        ]);
    } else {
        echo json_encode([
            "success" => true,
            "message" => "Database is already up to date. 'profile_pic' column already exists in the 'users' table."
        ]);
    }
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => "Failed to patch database: " . $e->getMessage()
    ]);
}
?>
