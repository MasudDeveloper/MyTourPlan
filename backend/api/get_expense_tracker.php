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

// Fetch Trip Budget
$stmt = $conn->prepare("SELECT budget FROM trips WHERE id = ? AND user_id = ?");
$stmt->execute([$trip_id, $user->user_id]);
$trip = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$trip) {
    echo json_encode(["error" => "Trip not found or access denied"]);
    exit();
}

// Fetch Expenses
$stmtExp = $conn->prepare("SELECT * FROM expenses WHERE trip_id = ? ORDER BY date DESC");
$stmtExp->execute([$trip_id]);
$expensesData = $stmtExp->fetchAll(PDO::FETCH_ASSOC);

$response = [
    "total_budget" => (float)$trip['budget'],
    "expenses" => []
];

foreach ($expensesData as $exp) {
    $response["expenses"][] = [
        "id" => (string)$exp['id'],
        "trip_id" => (string)$exp['trip_id'],
        "category" => $exp['category'],
        "amount" => (float)$exp['amount'],
        "note" => $exp['note'],
        "date" => $exp['date']
    ];
}

echo json_encode($response);
?>
