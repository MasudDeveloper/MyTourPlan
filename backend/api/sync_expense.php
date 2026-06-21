<?php
// sync_expense.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $trip_id = $_POST['trip_id'] ?? '';
    $category = $_POST['category'] ?? '';
    $amount = $_POST['amount'] ?? 0;
    $note = $_POST['note'] ?? '';
    $created_at = $_POST['created_at'] ?? date("Y-m-d H:i:s");
    $local_id = $_POST['local_id'] ?? '';
    $action = $_POST['action'] ?? 'INSERT';
    $server_id = $_POST['server_id'] ?? -1;

    try {
        if ($action == 'DELETE') {
            if ($server_id == -1) {
                echo json_encode(["error" => "Server ID required for deletion"]);
                exit;
            }
            $stmt = $conn->prepare("DELETE FROM expenses WHERE id = ?");
            $stmt->execute([$server_id]);
            echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Expense deleted"]);
            
        } else if ($action == 'UPDATE') {
            if ($server_id == -1) {
                echo json_encode(["error" => "Server ID required for update"]);
                exit;
            }
            $stmt = $conn->prepare("UPDATE expenses SET category = ?, amount = ?, note = ? WHERE id = ?");
            $stmt->execute([$category, $amount, $note, $server_id]);
            echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Expense updated"]);
            
        } else {
            // INSERT
            if (empty($trip_id) || empty($category)) {
                echo json_encode(["error" => "Trip ID and Category are required"]);
                exit;
            }
            $stmt = $conn->prepare("INSERT INTO expenses (trip_id, category, amount, note, date) VALUES (?, ?, ?, ?, ?)");
            $stmt->execute([$trip_id, $category, $amount, $note, $created_at]);
            $new_server_id = $conn->lastInsertId();
            echo json_encode(["success" => true, "server_id" => $new_server_id, "local_id" => $local_id, "message" => "Expense inserted"]);
        }
    } catch (PDOException $e) {
        echo json_encode(["error" => "Failed to sync expense: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Invalid Request Method"]);
}
?>
