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

$user_id = $user->user_id;

$stmt = $conn->prepare("SELECT * FROM trips WHERE user_id = ? ORDER BY start_date DESC");
$stmt->execute([$user_id]);
$trips = $stmt->fetchAll(PDO::FETCH_ASSOC);

$response = ["trips" => []];

foreach ($trips as $trip) {
    $response["trips"][] = [
        "id" => (string)$trip['id'],
        "user_id" => (string)$trip['user_id'],
        "from_location" => $trip['from_location'] ?? "",
        "destination" => $trip['destination'],
        "image_uri" => $trip['image_uri'] ?? "",
        "start_date" => $trip['start_date'],
        "end_date" => $trip['end_date'],
        "members_count" => (int)$trip['members_count'],
        "budget" => (float)$trip['budget'],
        "status" => $trip['status']
    ];
}

echo json_encode($response);
?>
