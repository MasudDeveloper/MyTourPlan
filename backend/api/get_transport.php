<?php
include_once 'config.php';

$from = isset($_GET['from']) ? $_GET['from'] : '';
$to = isset($_GET['to']) ? $_GET['to'] : '';

$query = "SELECT * FROM transport_options";
if ($from && $to) {
    $query .= " WHERE from_location = :from AND to_location = :to";
}
$query .= " ORDER BY price ASC";

$stmt = $conn->prepare($query);
if ($from && $to) {
    $stmt->bindParam(':from', $from);
    $stmt->bindParam(':to', $to);
}
$stmt->execute();

$transports = $stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode(array("transports" => $transports));
?>
