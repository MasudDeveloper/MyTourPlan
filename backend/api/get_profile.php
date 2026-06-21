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

// Fetch User
$stmt = $conn->prepare("SELECT id, name, email, phone, profile_pic FROM users WHERE id = ?");
$stmt->execute([$user->user_id]);
$userData = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$userData) {
    echo json_encode(["error" => "User not found"]);
    exit();
}

$response = [
    "id" => (int)$userData['id'],
    "name" => $userData['name'],
    "email" => $userData['email'],
    "phone" => $userData['phone'] ?? "",
    "profile_pic" => $userData['profile_pic'] ?? ""
];

echo json_encode($response);
?>
