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

$budgetPerPerson = (float)$trip['budget'];
$membersCount = (int)$trip['members_count'];
$totalBudget = $budgetPerPerson * $membersCount;

// Calculate Total Spent
$stmtExp = $conn->prepare("SELECT SUM(amount) as total_spent FROM expenses WHERE trip_id = ?");
$stmtExp->execute([$trip_id]);
$totalSpent = (float)$stmtExp->fetchColumn();

// Fetch Members to calculate Due
$stmtMembers = $conn->prepare("SELECT * FROM trip_members WHERE trip_id = ?");
$stmtMembers->execute([$trip_id]);
$membersData = $stmtMembers->fetchAll(PDO::FETCH_ASSOC);

$contributors = [];
foreach ($membersData as $member) {
    $amountPaid = (float)$member['amount_paid'];
    $totalDue = $budgetPerPerson - $amountPaid;
    $status = "Due";
    
    if ($totalDue < 0) {
        $status = "Refund";
    } else if ($totalDue == 0) {
        $status = "Paid";
    }

    $name = $member['name'];
    $parts = explode(" ", trim($name));
    if (count($parts) >= 2) {
        $initials = strtoupper(substr($parts[0], 0, 1) . substr($parts[1], 0, 1));
    } else {
        $initials = strtoupper(substr($name, 0, min(2, strlen($name))));
    }

    $contributors[] = [
        "id" => (string)$member['id'],
        "name" => $name,
        "initials" => $initials,
        "amount_paid" => $amountPaid,
        "total_due" => $totalDue,
        "status" => $status
    ];
}

$response = [
    "total_budget" => $totalBudget,
    "total_spent" => $totalSpent,
    "budget_string" => $membersCount . " Travelers x " . number_format($budgetPerPerson, 0) . " BDT",
    "contributors" => $contributors
];

echo json_encode($response);
?>
