<?php
// sync_trip.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_id = $_POST['user_id'] ?? '';
    $from_location = $_POST['from_location'] ?? '';
    $destination = $_POST['destination'] ?? '';
    $start_date = $_POST['start_date'] ?? '';
    $end_date = $_POST['end_date'] ?? '';
    $members_count = $_POST['members_count'] ?? 1;
    $budget = $_POST['budget'] ?? 0;
    $status = $_POST['status'] ?? 'Upcoming';
    $local_id = $_POST['local_id'] ?? ''; // App's local sqlite ID

    // Handle Image Upload
    $image_uri = "";
    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $target_dir = "uploads/trips/";
        if (!file_exists($target_dir)) {
            mkdir($target_dir, 0777, true);
        }
        $file_name = time() . "_" . basename($_FILES["image"]["name"]);
        $target_file = $target_dir . $file_name;

        if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
            // Adjust the domain base path as per your hosting setup
            $image_uri = "http://" . $_SERVER['HTTP_HOST'] . "/" . dirname($_SERVER['PHP_SELF']) . "/" . $target_file;
        }
    }

    if (empty($user_id) || empty($destination)) {
        echo json_encode(["error" => "User ID and Destination are required"]);
        exit;
    }

    try {
        $stmt = $conn->prepare("INSERT INTO trips (user_id, from_location, destination, start_date, end_date, members_count, budget, status, image_uri) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt->execute([$user_id, $from_location, $destination, $start_date, $end_date, $members_count, $budget, $status, $image_uri]);

        $server_id = $conn->lastInsertId();
        echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Trip synced successfully"]);
    } catch (PDOException $e) {
        echo json_encode(["error" => "Failed to sync trip: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Invalid Request Method"]);
}
