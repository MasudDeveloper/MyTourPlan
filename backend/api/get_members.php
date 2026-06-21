<?php
header("Content-Type: application/json");
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

// Fetch Trip Budget Info
$stmtTrip = $conn->prepare("SELECT budget, members_count FROM trips WHERE id = ?");
$stmtTrip->execute([$trip_id]);
$trip = $stmtTrip->fetch(PDO::FETCH_ASSOC);

if (!$trip) {
    echo json_encode(["error" => "Trip not found"]);
    exit();
}

// Fetch Members
$stmt = $conn->prepare("SELECT * FROM trip_members WHERE trip_id = ? ORDER BY id ASC");
$stmt->execute([$trip_id]);
$membersData = $stmt->fetchAll(PDO::FETCH_ASSOC);

$members = [];
foreach ($membersData as $item) {
    $members[] = [
        "id" => (string)$item['id'],
        "name" => $item['name'],
        "paid_amount" => (float)$item['amount_paid'],
        "payment_method" => $item['payment_method'] ?? ''
    ];
}

$response = [
    "budget_per_person" => (float)$trip['budget'],
    "members" => $members
];

echo json_encode($response);
?>
