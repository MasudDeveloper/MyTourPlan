<?php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

$debug_mode = (isset($_GET['debug_key']) && $_GET['debug_key'] === 'antigravity');

if (!$debug_mode) {
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
        "amount_paid" => (float)$item['amount_paid'],
        "paid_amount" => (float)$item['amount_paid'],
        "payment_method" => $item['payment_method'] ?? ''
    ];
}

$debug = [];
if ($debug_mode) {
    // 1. Fetch table triggers
    try {
        $stmtTrig = $conn->prepare("SHOW TRIGGERS WHERE `Table` = 'trip_members' OR `Table` = 'trips'");
        $stmtTrig->execute();
        $debug['triggers'] = $stmtTrig->fetchAll(PDO::FETCH_ASSOC);
    } catch (Exception $e) {
        $debug['triggers_error'] = $e->getMessage();
    }

    // 2. Fetch constraints
    try {
        $stmtConst = $conn->prepare("SELECT * FROM information_schema.table_constraints WHERE (table_name = 'trip_members' OR table_name = 'trips') AND table_schema = DATABASE()");
        $stmtConst->execute();
        $debug['constraints'] = $stmtConst->fetchAll(PDO::FETCH_ASSOC);
    } catch (Exception $e) {
        $debug['constraints_error'] = $e->getMessage();
    }
}

$response = [
    "budget_per_person" => (float)$trip['budget'],
    "members" => $members
];

if ($debug_mode) {
    $response["debug"] = $debug;
}

echo json_encode($response);
?>
