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

// Fetch Itinerary
$stmt = $conn->prepare("SELECT * FROM itinerary WHERE trip_id = ? ORDER BY day ASC, time ASC");
$stmt->execute([$trip_id]);
$itineraryData = $stmt->fetchAll(PDO::FETCH_ASSOC);

$response = ["itinerary" => []];

foreach ($itineraryData as $item) {
    $response["itinerary"][] = [
        "day" => (int)$item['day'],
        "time" => $item['time'],
        "activity" => $item['activity'],
        "location" => $item['location']
    ];
}

echo json_encode($response);
?>
