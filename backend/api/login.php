<?php
include_once 'config.php';
include_once 'jwt_helper.php';

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->email) && !empty($data->password)) {
    $query = "SELECT id, name, email, password FROM users WHERE email = :email LIMIT 1";
    $stmt = $conn->prepare($query);
    $email = htmlspecialchars(strip_tags($data->email));
    $stmt->bindParam(":email", $email);
    $stmt->execute();

    if($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $password_hash = $row['password'];

        if(password_verify($data->password, $password_hash)) {
            $token = generateJWT($row['id'], $row['email'], $jwt_secret);
            
            http_response_code(200);
            echo json_encode(array(
                "message" => "Login successful.",
                "token" => $token,
                "user" => array(
                    "id" => $row['id'],
                    "name" => $row['name'],
                    "email" => $row['email']
                )
            ));
        } else {
            http_response_code(401);
            echo json_encode(array("error" => "Invalid credentials."));
        }
    } else {
        http_response_code(401);
        echo json_encode(array("error" => "Invalid credentials."));
    }
} else {
    http_response_code(400);
    echo json_encode(array("error" => "Incomplete data."));
}
?>
