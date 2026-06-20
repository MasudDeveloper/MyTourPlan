<?php
include_once 'config.php';
include_once 'jwt_helper.php';

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->name) && !empty($data->email) && !empty($data->password)) {
    
    // Check if email exists
    $check_query = "SELECT id FROM users WHERE email = :email";
    $stmt = $conn->prepare($check_query);
    $stmt->bindParam(':email', $data->email);
    $stmt->execute();
    
    if($stmt->rowCount() > 0) {
        http_response_code(400);
        echo json_encode(array("error" => "Email already exists."));
        exit();
    }

    $query = "INSERT INTO users (name, email, phone, password) VALUES (:name, :email, :phone, :password)";
    $stmt = $conn->prepare($query);

    $name = htmlspecialchars(strip_tags($data->name));
    $email = htmlspecialchars(strip_tags($data->email));
    $phone = isset($data->phone) ? htmlspecialchars(strip_tags($data->phone)) : null;
    $password_hash = password_hash($data->password, PASSWORD_BCRYPT);

    $stmt->bindParam(":name", $name);
    $stmt->bindParam(":email", $email);
    $stmt->bindParam(":phone", $phone);
    $stmt->bindParam(":password", $password_hash);

    if($stmt->execute()) {
        $user_id = $conn->lastInsertId();
        $token = generateJWT($user_id, $email, $jwt_secret);
        
        http_response_code(201);
        echo json_encode(array(
            "message" => "User registered successfully.",
            "token" => $token,
            "user" => array(
                "id" => $user_id,
                "name" => $name,
                "email" => $email
            )
        ));
    } else {
        http_response_code(503);
        echo json_encode(array("error" => "Unable to register user."));
    }
} else {
    http_response_code(400);
    echo json_encode(array("error" => "Incomplete data."));
}
?>
