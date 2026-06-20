<?php
// update_profile.php
header("Content-Type: application/json");
require_once 'config.php';
require_once 'jwt_helper.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_id = $_POST['user_id'] ?? '';
    $name = $_POST['name'] ?? '';
    $phone = $_POST['phone'] ?? '';

    // Handle Profile Image Upload
    $profile_pic = "";
    if (isset($_FILES['profile_pic']) && $_FILES['profile_pic']['error'] === UPLOAD_ERR_OK) {
        $target_dir = "uploads/profiles/";
        if (!file_exists($target_dir)) {
            mkdir($target_dir, 0777, true);
        }
        $file_name = time() . "_" . basename($_FILES["profile_pic"]["name"]);
        $target_file = $target_dir . $file_name;

        if (move_uploaded_file($_FILES["profile_pic"]["tmp_name"], $target_file)) {
            $profile_pic = "http://" . $_SERVER['HTTP_HOST'] . "/" . dirname($_SERVER['PHP_SELF']) . "/" . $target_file;
        }
    }

    if (empty($user_id)) {
        echo json_encode(["error" => "User ID is required"]);
        exit;
    }

    try {
        if (!empty($profile_pic)) {
            $stmt = $conn->prepare("UPDATE users SET name = ?, phone = ?, profile_pic = ? WHERE id = ?");
            $stmt->execute([$name, $phone, $profile_pic, $user_id]);
        } else {
            $stmt = $conn->prepare("UPDATE users SET name = ?, phone = ? WHERE id = ?");
            $stmt->execute([$name, $phone, $user_id]);
        }

        echo json_encode(["success" => true, "profile_pic" => $profile_pic, "message" => "Profile updated successfully"]);
    } catch (PDOException $e) {
        echo json_encode(["error" => "Failed to update profile: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["error" => "Invalid Request Method"]);
}
