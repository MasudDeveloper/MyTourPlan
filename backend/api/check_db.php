<?php
header("Content-Type: application/json");
require_once 'config.php';

try {
    $stmt = $conn->query("SHOW COLUMNS FROM itinerary");
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode([
        "success" => true,
        "columns" => $columns
    ]);
} catch (PDOException $e) {
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ]);
}
?>
