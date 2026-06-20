<?php
// sync_itinerary.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $trip_id = $_POST['trip_id'] ?? '';
    $day = $_POST['day'] ?? 1;
    $time = $_POST['time'] ?? '';
    $activity = $_POST['activity'] ?? '';
    $location = $_POST['location'] ?? '';
    $local_id = $_POST['local_id'] ?? '';

    if (empty($trip_id) || empty($activity)) {
        echo json_encode(["error" => "Trip ID and Activity are required"]);
        exit;
    }

    try {
        $stmt = $conn->prepare("INSERT INTO itinerary (trip_id, day, time, activity, location) VALUES (?, ?, ?, ?, ?)");
        $stmt->execute([$trip_id, $day, $time, $activity, $location]);

        $server_id = $conn->lastInsertId();
        echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Itinerary synced successfully"]);
    } catch (PDOException $e) {
        echo json_encode(["error" => "Failed to sync itinerary: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Invalid Request Method"]);
}
