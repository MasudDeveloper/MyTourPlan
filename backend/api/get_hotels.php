<?php
include_once 'config.php';

$location = isset($_GET['location']) ? $_GET['location'] : '';

$query = "SELECT * FROM hotels";
if ($location) {
    $query .= " WHERE location = :location";
}
$query .= " ORDER BY price_per_night ASC";

$stmt = $conn->prepare($query);
if ($location) {
    $stmt->bindParam(':location', $location);
}
$stmt->execute();

$hotels = $stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode(array("hotels" => $hotels));
?>
