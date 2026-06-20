<?php
include_once 'config.php';
include_once 'jwt_helper.php';

$token = getBearerToken();
$user = validateJWT($token, $jwt_secret);

if (!$user) {
    http_response_code(401);
    echo json_encode(array("error" => "Unauthorized."));
    exit();
}

$query = "SELECT * FROM trip_plans WHERE user_id = :user_id ORDER BY created_at DESC";
$stmt = $conn->prepare($query);
$stmt->bindParam(':user_id', $user->user_id);
$stmt->execute();

$trips = $stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode(array("trips" => $trips));
?>
