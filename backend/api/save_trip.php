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

$data = json_decode(file_get_contents("php://input"));

if (!empty($data->from_location) && !empty($data->to_location) && !empty($data->total_cost)) {
    try {
        $conn->beginTransaction();

        $query = "INSERT INTO trip_plans (user_id, from_location, to_location, days, persons, budget, total_cost) 
                  VALUES (:user_id, :from, :to, :days, :persons, :budget, :total_cost)";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(':user_id', $user->user_id);
        $stmt->bindParam(':from', $data->from_location);
        $stmt->bindParam(':to', $data->to_location);
        $stmt->bindParam(':days', $data->days);
        $stmt->bindParam(':persons', $data->persons);
        $stmt->bindParam(':budget', $data->budget);
        $stmt->bindParam(':total_cost', $data->total_cost);
        $stmt->execute();

        $plan_id = $conn->lastInsertId();

        // Insert expenses
        if (isset($data->costs)) {
            $exp_query = "INSERT INTO trip_expenses (plan_id, category, title, cost) VALUES (:plan_id, :cat, :title, :cost)";
            $exp_stmt = $conn->prepare($exp_query);
            foreach ($data->costs as $key => $val) {
                if ($key !== "total_estimated_cost" && $key !== "budget_status") {
                    $exp_stmt->execute([
                        ':plan_id' => $plan_id,
                        ':cat' => $key,
                        ':title' => ucfirst(str_replace('_', ' ', $key)),
                        ':cost' => $val
                    ]);
                }
            }
        }

        // Insert itinerary
        if (isset($data->itinerary)) {
            $it_query = "INSERT INTO itinerary (plan_id, day_number, time, activity, location) VALUES (:plan_id, :day, :time, :activity, :location)";
            $it_stmt = $conn->prepare($it_query);
            foreach ($data->itinerary as $item) {
                $it_stmt->execute([
                    ':plan_id' => $plan_id,
                    ':day' => $item->day,
                    ':time' => $item->time,
                    ':activity' => $item->activity,
                    ':location' => $item->location
                ]);
            }
        }

        $conn->commit();

        http_response_code(201);
        echo json_encode(array("message" => "Trip saved successfully.", "plan_id" => $plan_id));

    } catch (Exception $e) {
        $conn->rollBack();
        http_response_code(500);
        echo json_encode(array("error" => "Failed to save trip: " . $e->getMessage()));
    }
} else {
    http_response_code(400);
    echo json_encode(array("error" => "Incomplete data."));
}
?>
