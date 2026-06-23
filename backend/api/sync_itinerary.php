<?php
// sync_itinerary.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $token = getBearerToken();
    $user = validateJWT($token, $jwt_secret);

    if (!$user) {
        http_response_code(401);
        echo json_encode(array("success" => false, "error" => "Unauthorized"));
        exit();
    }

    $trip_id = $_POST['trip_id'] ?? '';
    $day = $_POST['day'] ?? 1;
    $time = $_POST['time'] ?? '';
    $activity = $_POST['activity'] ?? '';
    $location = $_POST['location'] ?? '';
    $local_id = $_POST['local_id'] ?? '';

    $action = $_POST['action'] ?? 'INSERT';
    $server_id = $_POST['server_id'] ?? -1;

    if (empty($trip_id)) {
        echo json_encode(["success" => false, "error" => "Trip ID is required"]);
        exit;
    }

    try {
        // Validate trip ownership
        $checkTrip = $conn->prepare("SELECT id FROM trips WHERE id = ? AND user_id = ?");
        $checkTrip->execute([$trip_id, $user->user_id]);
        if (!$checkTrip->fetch()) {
            echo json_encode(["success" => false, "error" => "Invalid Trip ID or Permission Denied"]);
            exit;
        }

        if ($action == 'DELETE') {
            if ($server_id == -1) {
                echo json_encode(["success" => false, "error" => "Server ID required for deletion"]);
                exit;
            }

            // Verify itinerary item belongs to the given trip
            $checkItinerary = $conn->prepare("SELECT id FROM itinerary WHERE id = ? AND trip_id = ?");
            $checkItinerary->execute([$server_id, $trip_id]);
            if (!$checkItinerary->fetch()) {
                echo json_encode(["success" => false, "error" => "Itinerary item not found or does not belong to this trip"]);
                exit;
            }

            $stmt = $conn->prepare("DELETE FROM itinerary WHERE id = ?");
            $stmt->execute([$server_id]);
            echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Itinerary deleted"]);
            
        } else if ($action == 'UPDATE') {
            if ($server_id == -1) {
                echo json_encode(["success" => false, "error" => "Server ID required for update"]);
                exit;
            }

            // Verify itinerary item belongs to the given trip
            $checkItinerary = $conn->prepare("SELECT id FROM itinerary WHERE id = ? AND trip_id = ?");
            $checkItinerary->execute([$server_id, $trip_id]);
            if (!$checkItinerary->fetch()) {
                echo json_encode(["success" => false, "error" => "Itinerary item not found or does not belong to this trip"]);
                exit;
            }

            if (empty($day) || empty($time) || empty($activity)) {
                echo json_encode(["success" => false, "error" => "Day, Time, and Activity are required"]);
                exit;
            }

            $stmt = $conn->prepare("UPDATE itinerary SET day = ?, time = ?, activity = ?, location = ? WHERE id = ?");
            $stmt->execute([$day, $time, $activity, $location, $server_id]);
            echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Itinerary updated"]);
            
        } else {
            // INSERT
            if (empty($day) || empty($time) || empty($activity)) {
                echo json_encode(["success" => false, "error" => "Day, Time, and Activity are required"]);
                exit;
            }

            $stmt = $conn->prepare("INSERT INTO itinerary (trip_id, day, time, activity, location) VALUES (?, ?, ?, ?, ?)");
            $stmt->execute([$trip_id, $day, $time, $activity, $location]);

            $new_server_id = $conn->lastInsertId();
            echo json_encode(["success" => true, "server_id" => $new_server_id, "local_id" => $local_id, "message" => "Itinerary synced successfully"]);
        }
    } catch (PDOException $e) {
        echo json_encode(["success" => false, "error" => "Failed to sync itinerary: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["success" => false, "error" => "Invalid Request Method"]);
}
?>
