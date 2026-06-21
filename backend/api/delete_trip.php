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

if (!empty($data->trip_id)) {
    try {
        $query = "DELETE FROM trips WHERE id = :trip_id AND user_id = :user_id";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(':trip_id', $data->trip_id);
        $stmt->bindParam(':user_id', $user->user_id);
        $stmt->execute();

        if ($stmt->rowCount() > 0) {
            http_response_code(200);
            echo json_encode(array("message" => "Trip deleted successfully."));
        } else {
            http_response_code(404);
            echo json_encode(array("error" => "Trip not found or unauthorized."));
        }
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(array("error" => "Failed to delete trip: " . $e->getMessage()));
    }
} else {
    http_response_code(400);
    echo json_encode(array("error" => "Trip ID is required."));
}
?>
