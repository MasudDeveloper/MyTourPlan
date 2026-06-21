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

$trip_id = isset($_GET['trip_id']) ? $_GET['trip_id'] : null;

if (!$trip_id) {
    echo json_encode(["error" => "Trip ID required"]);
    exit();
}

// Fetch Trip Details
$stmtTrip = $conn->prepare("SELECT * FROM trips WHERE id = ?");
$stmtTrip->execute([$trip_id]);
$trip = $stmtTrip->fetch(PDO::FETCH_ASSOC);

if (!$trip) {
    echo json_encode(["error" => "Trip not found"]);
    exit();
}

// Calculate total budget
$total_budget = $trip['budget'] * $trip['members_count'];

// Calculate duration string (simple version)
$duration = $trip['start_date'] . " to " . $trip['end_date'];

// Fetch Itinerary
$stmt = $conn->prepare("SELECT * FROM itinerary WHERE trip_id = ? ORDER BY day ASC, time ASC");
$stmt->execute([$trip_id]);
$itineraryData = $stmt->fetchAll(PDO::FETCH_ASSOC);

$schedule = [];
foreach ($itineraryData as $item) {
    $schedule[] = [
        "id" => (string)$item['id'],
        "day" => (int)$item['day'],
        "time" => $item['time'],
        "activity" => $item['activity'],
        "location" => $item['location']
    ];
}

$response = [
    "total_budget" => "৳" . number_format($total_budget, 2),
    "duration" => $duration,
    "travelers_count" => $trip['members_count'] . " Members",
    "schedule" => $schedule
];

echo json_encode($response);
?>
