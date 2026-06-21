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

// Fetch Trip
$stmt = $conn->prepare("SELECT * FROM trips WHERE id = ? AND user_id = ?");
$stmt->execute([$trip_id, $user->user_id]);
$trip = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$trip) {
    echo json_encode(["error" => "Trip not found or access denied"]);
    exit();
}

// Fetch Expenses
$stmtExp = $conn->prepare("SELECT * FROM expenses WHERE trip_id = ? ORDER BY id DESC");
$stmtExp->execute([$trip_id]);
$expensesData = $stmtExp->fetchAll(PDO::FETCH_ASSOC);

$expenses = [];
foreach ($expensesData as $exp) {
    $expenses[] = [
        "id" => (string)$exp['id'],
        "category" => $exp['category'],
        "amount" => (float)$exp['amount'],
        "note" => $exp['note'] ?? ''
    ];
}

$response = [
    "trip" => [
        "id" => (string)$trip['id'],
        "destination" => $trip['destination'],
        "start_date" => $trip['start_date'],
        "end_date" => $trip['end_date'],
        "members_count" => (int)$trip['members_count'],
        "budget" => (float)$trip['budget'],
        "image_uri" => $trip['image_uri'] ?? ''
    ],
    "expenses" => $expenses
];

echo json_encode($response);
?>
