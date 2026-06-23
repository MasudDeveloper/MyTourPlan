<?php
// sync_notes_checklist.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $token = getBearerToken();
    $user = validateJWT($token, $jwt_secret);

    if (!$user) {
        http_response_code(401);
        echo json_encode(["success" => false, "error" => "Unauthorized"]);
        exit();
    }

    $trip_id = $_POST['trip_id'] ?? '';
    $type = $_POST['type'] ?? ''; // "NOTE" or "CHECKLIST"
    $action = $_POST['action'] ?? 'INSERT'; // "INSERT", "UPDATE", "DELETE", "TOGGLE"
    $id = $_POST['id'] ?? -1;

    $title = $_POST['title'] ?? '';
    $content = $_POST['content'] ?? '';
    $is_checked = $_POST['is_checked'] ?? 0;

    if (empty($trip_id) || empty($type)) {
        echo json_encode(["success" => false, "error" => "Trip ID and Type are required"]);
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

        if ($type == 'NOTE') {
            if ($action == 'DELETE') {
                if ($id == -1) {
                    echo json_encode(["success" => false, "error" => "Note ID required for deletion"]);
                    exit();
                }
                $stmt = $conn->prepare("DELETE FROM trip_notes WHERE id = ? AND trip_id = ?");
                $stmt->execute([$id, $trip_id]);
                echo json_encode(["success" => true, "message" => "Note deleted successfully", "id" => (int)$id]);

            } else if ($action == 'UPDATE') {
                if ($id == -1) {
                    echo json_encode(["success" => false, "error" => "Note ID required for update"]);
                    exit();
                }
                if (empty($title)) {
                    echo json_encode(["success" => false, "error" => "Title is required"]);
                    exit();
                }
                $stmt = $conn->prepare("UPDATE trip_notes SET title = ?, content = ? WHERE id = ? AND trip_id = ?");
                $stmt->execute([$title, $content, $id, $trip_id]);
                echo json_encode(["success" => true, "message" => "Note updated successfully", "id" => (int)$id]);

            } else {
                // INSERT
                if (empty($title)) {
                    echo json_encode(["success" => false, "error" => "Title is required"]);
                    exit();
                }
                $stmt = $conn->prepare("INSERT INTO trip_notes (trip_id, title, content) VALUES (?, ?, ?)");
                $stmt->execute([$trip_id, $title, $content]);
                $new_id = $conn->lastInsertId();
                echo json_encode(["success" => true, "message" => "Note added successfully", "id" => (int)$new_id]);
            }
        } else if ($type == 'CHECKLIST') {
            if ($action == 'DELETE') {
                if ($id == -1) {
                    echo json_encode(["success" => false, "error" => "Checklist Item ID required for deletion"]);
                    exit();
                }
                $stmt = $conn->prepare("DELETE FROM trip_checklists WHERE id = ? AND trip_id = ?");
                $stmt->execute([$id, $trip_id]);
                echo json_encode(["success" => true, "message" => "Checklist item deleted successfully", "id" => (int)$id]);

            } else if ($action == 'TOGGLE') {
                if ($id == -1) {
                    echo json_encode(["success" => false, "error" => "Checklist Item ID required for toggle"]);
                    exit();
                }
                $stmt = $conn->prepare("UPDATE trip_checklists SET is_checked = ? WHERE id = ? AND trip_id = ?");
                $stmt->execute([$is_checked, $id, $trip_id]);
                echo json_encode(["success" => true, "message" => "Checklist item toggled successfully", "id" => (int)$id]);

            } else if ($action == 'UPDATE') {
                if ($id == -1) {
                    echo json_encode(["success" => false, "error" => "Checklist Item ID required for update"]);
                    exit();
                }
                if (empty($title)) {
                    echo json_encode(["success" => false, "error" => "Title is required"]);
                    exit();
                }
                $stmt = $conn->prepare("UPDATE trip_checklists SET title = ? WHERE id = ? AND trip_id = ?");
                $stmt->execute([$title, $id, $trip_id]);
                echo json_encode(["success" => true, "message" => "Checklist item updated successfully", "id" => (int)$id]);

            } else {
                // INSERT
                if (empty($title)) {
                    echo json_encode(["success" => false, "error" => "Title is required"]);
                    exit();
                }
                $stmt = $conn->prepare("INSERT INTO trip_checklists (trip_id, title, is_checked) VALUES (?, ?, 0)");
                $stmt->execute([$trip_id, $title]);
                $new_id = $conn->lastInsertId();
                echo json_encode(["success" => true, "message" => "Checklist item added successfully", "id" => (int)$new_id]);
            }
        } else {
            echo json_encode(["success" => false, "error" => "Invalid Type"]);
        }

    } catch (PDOException $e) {
        echo json_encode(["success" => false, "error" => "Database error: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["success" => false, "error" => "Invalid Request Method"]);
}
?>
