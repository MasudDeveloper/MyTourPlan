<?php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $token = getBearerToken();
    $user = validateJWT($token, $jwt_secret);

    if (!$user) {
        http_response_code(401);
        echo json_encode(array("error" => "Unauthorized"));
        exit();
    }

    $trip_id = $_POST['trip_id'] ?? '';
    $from_location = $_POST['from_location'] ?? '';
    $destination = $_POST['destination'] ?? '';
    $start_date = $_POST['start_date'] ?? '';
    $end_date = $_POST['end_date'] ?? '';
    $members_count = $_POST['members_count'] ?? 1;
    $budget = $_POST['budget'] ?? 0;
    $status = $_POST['status'] ?? 'Upcoming';

    if (empty($trip_id) || empty($destination)) {
        echo json_encode(["error" => "Trip ID and Destination are required"]);
        exit;
    }

    // Handle Image Upload
    $image_query = "";
    $params = [$from_location, $destination, $start_date, $end_date, $members_count, $budget, $status];

    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $target_dir = "uploads/trips/";
        if (!file_exists($target_dir)) {
            mkdir($target_dir, 0777, true);
        }
        $file_name = time() . "_" . basename($_FILES["image"]["name"]);
        $target_file = $target_dir . $file_name;

        if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
            $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
            $image_uri = $protocol . "://" . $_SERVER['HTTP_HOST'] . rtrim(dirname($_SERVER['PHP_SELF']), '/\\') . "/" . $target_file;
            $image_query = ", image_uri = ?";
            $params[] = $image_uri;
        }
    }

    $params[] = $trip_id;
    $params[] = $user->user_id;

    try {
        $stmt = $conn->prepare("UPDATE trips SET from_location = ?, destination = ?, start_date = ?, end_date = ?, members_count = ?, budget = ?, status = ? $image_query WHERE id = ? AND user_id = ?");
        $stmt->execute($params);

        echo json_encode(["success" => true, "message" => "Trip updated successfully"]);
    } catch (PDOException $e) {
        echo json_encode(["error" => "Failed to update trip: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Invalid Request Method"]);
}
?>
