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

// Get User Name
$stmt = $conn->prepare("SELECT name FROM users WHERE id = ?");
$stmt->execute([$user_id]);
$userData = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$userData) {
    echo json_encode(["error" => "User not found"]);
    exit();
}

// Get Upcoming Trip
$stmtTrip = $conn->prepare("SELECT * FROM trips WHERE user_id = ? AND status = 'Upcoming' ORDER BY start_date ASC LIMIT 1");
$stmtTrip->execute([$user_id]);
$tripData = $stmtTrip->fetch(PDO::FETCH_ASSOC);

$response = [
    "user_name" => $userData['name'],
    "upcoming_trip" => null
];

if ($tripData) {
    $response["upcoming_trip"] = [
        "id" => (string)$tripData['id'],
        "user_id" => (string)$tripData['user_id'],
        "from_location" => $tripData['from_location'] ?? "",
        "destination" => $tripData['destination'],
        "image_uri" => $tripData['image_uri'] ?? "",
        "start_date" => $tripData['start_date'],
        "end_date" => $tripData['end_date'],
        "members_count" => (int)$tripData['members_count'],
        "budget" => (float)$tripData['budget'],
        "status" => $tripData['status']
    ];
}

echo json_encode($response);
?>
