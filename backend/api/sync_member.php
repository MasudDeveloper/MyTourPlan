<?php
// sync_member.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $trip_id = $_POST['trip_id'] ?? '';
    $name = $_POST['name'] ?? '';
    $amount_paid = $_POST['amount_paid'] ?? 0;
    $payment_method = $_POST['payment_method'] ?? 'Cash';
    $local_id = $_POST['local_id'] ?? '';
    $action = $_POST['action'] ?? 'INSERT';
    $server_id = $_POST['server_id'] ?? -1;

    try {
        if ($action == 'DELETE') {
            if ($server_id == -1) {
                echo json_encode(["error" => "Server ID required for deletion"]);
                exit;
            }
            $stmt = $conn->prepare("DELETE FROM trip_members WHERE id = ?");
            $stmt->execute([$server_id]);
            echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Member deleted"]);
            
        } else if ($action == 'UPDATE') {
            if ($server_id == -1) {
                echo json_encode(["error" => "Server ID required for update"]);
                exit;
            }
            $stmt = $conn->prepare("UPDATE trip_members SET name = ?, amount_paid = ?, payment_method = ? WHERE id = ?");
            $stmt->execute([$name, $amount_paid, $payment_method, $server_id]);
            echo json_encode(["success" => true, "server_id" => $server_id, "local_id" => $local_id, "message" => "Member updated"]);
            
        } else {
            // INSERT
            if (empty($trip_id) || empty($name)) {
                echo json_encode(["error" => "Trip ID and Name are required"]);
                exit;
            }
            $stmt = $conn->prepare("INSERT INTO trip_members (trip_id, name, amount_paid, payment_method) VALUES (?, ?, ?, ?)");
            $stmt->execute([$trip_id, $name, $amount_paid, $payment_method]);
            $new_server_id = $conn->lastInsertId();
            echo json_encode(["success" => true, "server_id" => $new_server_id, "local_id" => $local_id, "message" => "Member inserted"]);
        }
    } catch (PDOException $e) {
        echo json_encode(["error" => "Failed to sync member: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Invalid Request Method"]);
}
?>
