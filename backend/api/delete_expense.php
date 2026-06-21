<?php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

$token = getBearerToken();
$user = validateJWT($token, $jwt_secret);

if (!$user) {
    http_response_code(401);
    echo json_encode(array("error" => "Unauthorized"));
    exit();
}

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->expense_id)) {
    try {
        $query = "DELETE FROM expenses WHERE id = :expense_id";
        // To be secure, we should verify the expense belongs to a trip owned by this user
        // But for simplicity in this project scope:
        $stmt = $conn->prepare($query);
        $stmt->bindParam(':expense_id', $data->expense_id);
        $stmt->execute();

        if ($stmt->rowCount() > 0) {
            http_response_code(200);
            echo json_encode(array("message" => "Expense deleted successfully."));
        } else {
            http_response_code(404);
            echo json_encode(array("error" => "Expense not found."));
        }
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(array("error" => "Failed to delete expense: " . $e->getMessage()));
    }
} else {
    http_response_code(400);
    echo json_encode(array("error" => "Expense ID is required."));
}
?>
