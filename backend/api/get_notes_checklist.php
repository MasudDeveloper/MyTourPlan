<?php
// get_notes_checklist.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

$token = getBearerToken();
if (!$token) {
    echo json_encode(["success" => false, "error" => "Unauthorized"]);
    exit();
}

$user = validateJWT($token, $jwt_secret);
if (!$user) {
    echo json_encode(["success" => false, "error" => "Invalid Token"]);
    exit();
}

$trip_id = isset($_GET['trip_id']) ? $_GET['trip_id'] : null;

if (!$trip_id) {
    echo json_encode(["success" => false, "error" => "Trip ID is required"]);
    exit();
}

try {
    // Verify Trip Ownership
    $stmtTrip = $conn->prepare("SELECT id FROM trips WHERE id = ? AND user_id = ?");
    $stmtTrip->execute([$trip_id, $user->user_id]);
    $trip = $stmtTrip->fetch(PDO::FETCH_ASSOC);

    if (!$trip) {
        echo json_encode(["success" => false, "error" => "Trip not found or permission denied"]);
        exit();
    }

    // Fetch Trip Notes
    $stmtNotes = $conn->prepare("SELECT * FROM trip_notes WHERE trip_id = ? ORDER BY id DESC");
    $stmtNotes->execute([$trip_id]);
    $notesData = $stmtNotes->fetchAll(PDO::FETCH_ASSOC);
    $notes = [];
    foreach ($notesData as $row) {
        $notes[] = [
            "id" => (int)$row['id'],
            "trip_id" => (string)$row['trip_id'],
            "title" => $row['title'],
            "content" => $row['content'] ?? '',
            "created_at" => $row['created_at']
        ];
    }

    // Fetch Trip Checklist
    $stmtCheck = $conn->prepare("SELECT * FROM trip_checklists WHERE trip_id = ? ORDER BY id DESC");
    $stmtCheck->execute([$trip_id]);
    $checkData = $stmtCheck->fetchAll(PDO::FETCH_ASSOC);
    $checklist = [];
    foreach ($checkData as $row) {
        $checklist[] = [
            "id" => (int)$row['id'],
            "trip_id" => (string)$row['trip_id'],
            "title" => $row['title'],
            "is_checked" => (int)$row['is_checked']
        ];
    }

    echo json_encode([
        "success" => true,
        "notes" => $notes,
        "checklist" => $checklist
    ]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "error" => "Database error: " . $e->getMessage()]);
}
?>
