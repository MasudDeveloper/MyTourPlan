<?php
require_once 'config.php';
require_once 'jwt_helper.php';

$token = getBearerToken();
if (!$token) {
    echo json_encode(["error" => "Unauthorized"]);
    exit();
}

$user = validateJWT($token, $jwt_secret);
if (!$user) {
    echo json_encode(["error" => "Invalid Token"]);
    exit();
}

$data = json_decode(file_get_contents("php://input"), true);
$user_id = $user->user_id;

$response = [
    "synced_trips" => [],
    "synced_expenses" => []
];

// Start transaction for atomicity
$conn->beginTransaction();

try {
    // 1. Process Trips
    $tripIdMapping = []; // map local_id to server_id
    
    if (isset($data['trips']) && is_array($data['trips'])) {
        $stmtTrip = $conn->prepare("INSERT INTO trips (user_id, destination, start_date, end_date, members_count, budget, status) VALUES (?, ?, ?, ?, ?, ?, ?)");
        
        foreach ($data['trips'] as $trip) {
            $stmtTrip->execute([
                $user_id,
                $trip['destination'],
                $trip['start_date'] ?? '',
                $trip['end_date'] ?? '',
                $trip['members_count'] ?? 1,
                $trip['budget'] ?? 0,
                $trip['status'] ?? 'Upcoming'
            ]);
            $server_id = $conn->lastInsertId();
            
            $local_id = $trip['id']; // This is the SQLite ID
            $tripIdMapping[$local_id] = $server_id;
            
            $response["synced_trips"][] = [
                "local_id" => (string)$local_id,
                "server_id" => (string)$server_id
            ];
        }
    }

    // 2. Process Expenses
    if (isset($data['expenses']) && is_array($data['expenses'])) {
        $stmtExp = $conn->prepare("INSERT INTO expenses (trip_id, category, amount, note) VALUES (?, ?, ?, ?)");
        
        foreach ($data['expenses'] as $exp) {
            $local_trip_id = $exp['trip_id'];
            
            // If the trip was just synced, use its new server_id. Otherwise use existing.
            $server_trip_id = isset($tripIdMapping[$local_trip_id]) ? $tripIdMapping[$local_trip_id] : $local_trip_id;

            $stmtExp->execute([
                $server_trip_id,
                $exp['category'],
                $exp['amount'] ?? 0,
                $exp['note'] ?? ''
            ]);
            $server_id = $conn->lastInsertId();
            
            $response["synced_expenses"][] = [
                "local_id" => (string)$exp['id'],
                "server_id" => (string)$server_id
            ];
        }
    }

    $conn->commit();
    echo json_encode($response);

} catch (Exception $e) {
    $conn->rollBack();
    echo json_encode(["error" => "Sync failed: " . $e->getMessage()]);
}
?>
