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

if (!empty($data->from_location) && !empty($data->to_location) && !empty($data->days) && !empty($data->persons) && !empty($data->budget)) {
    
    $from = $data->from_location;
    $to = $data->to_location;
    $days = (int)$data->days;
    $nights = $days > 1 ? $days - 1 : 0;
    $persons = (int)$data->persons;
    $budget = (float)$data->budget;

    // Fetch Transports
    $stmt = $conn->prepare("SELECT * FROM transport_options WHERE from_location = :from AND to_location = :to ORDER BY price ASC");
    $stmt->bindParam(':from', $from);
    $stmt->bindParam(':to', $to);
    $stmt->execute();
    $transports = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($transports) == 0) {
        $transports = [
            ["type" => "Generic Bus", "price" => 1500, "duration" => "8 hours"]
        ];
    }

    // Fetch Hotels
    $stmt = $conn->prepare("SELECT * FROM hotels WHERE location = :to ORDER BY price_per_night ASC");
    $stmt->bindParam(':to', $to);
    $stmt->execute();
    $hotels = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($hotels) == 0) {
        $hotels = [
            ["name" => "Generic Hotel", "price_per_night" => 2000, "rating" => 4.0, "type" => "Medium"]
        ];
    }

    // Determine target category based on budget per person
    $budget_per_person = $budget / $persons;
    
    $selected_transport = null;
    $selected_hotel = null;

    // Smart Budget Optimizer (Simple Logic)
    if ($budget_per_person < 5000) {
        $selected_transport = $transports[0]; // Cheapest
        $selected_hotel = $hotels[0]; // Cheapest
        $food_per_day = 500;
        $entry_fees = 500;
    } else if ($budget_per_person < 15000) {
        $selected_transport = $transports[min(1, count($transports) - 1)]; // Medium
        $selected_hotel = $hotels[min(1, count($hotels) - 1)]; // Medium
        $food_per_day = 1000;
        $entry_fees = 1000;
    } else {
        $selected_transport = $transports[count($transports) - 1]; // Premium
        $selected_hotel = $hotels[count($hotels) - 1]; // Premium
        $food_per_day = 2000;
        $entry_fees = 2000;
    }

    $transport_cost = $selected_transport['price'] * $persons * 2; // Round trip
    $hotel_cost = $selected_hotel['price_per_night'] * $nights * ceil($persons / 2); // Assume 2 persons per room
    $food_cost = $food_per_day * $days * $persons;
    $guide_cost = 1000 * $days; // Flat guide cost per day

    $subtotal = $transport_cost + $hotel_cost + $food_cost + $entry_fees + $guide_cost;
    $emergency_buffer = $subtotal * 0.10;
    $total_cost = $subtotal + $emergency_buffer;

    // Generate Itinerary
    $itinerary = [];
    for ($i = 1; $i <= $days; $i++) {
        if ($i == 1) {
            $itinerary[] = ["day" => $i, "time" => "08:00 AM", "activity" => "Departure from " . $from, "location" => $from];
            $itinerary[] = ["day" => $i, "time" => "02:00 PM", "activity" => "Arrival and Hotel check-in", "location" => $selected_hotel['name']];
            $itinerary[] = ["day" => $i, "time" => "04:00 PM", "activity" => "Local sightseeing / Beach walk", "location" => $to];
            $itinerary[] = ["day" => $i, "time" => "08:00 PM", "activity" => "Dinner and Rest", "location" => "Local Restaurant"];
        } else if ($i == $days) {
            $itinerary[] = ["day" => $i, "time" => "06:00 AM", "activity" => "Sunrise view", "location" => $to];
            $itinerary[] = ["day" => $i, "time" => "11:00 AM", "activity" => "Hotel check-out and Shopping", "location" => "Local Market"];
            $itinerary[] = ["day" => $i, "time" => "02:00 PM", "activity" => "Return Journey to " . $from, "location" => $to];
        } else {
            $itinerary[] = ["day" => $i, "time" => "09:00 AM", "activity" => "Breakfast", "location" => $selected_hotel['name']];
            $itinerary[] = ["day" => $i, "time" => "10:00 AM", "activity" => "Visit major tourist spots in " . $to, "location" => "Tourist Spots"];
            $itinerary[] = ["day" => $i, "time" => "01:00 PM", "activity" => "Lunch", "location" => "Local Restaurant"];
            $itinerary[] = ["day" => $i, "time" => "03:00 PM", "activity" => "Adventure activities or more sightseeing", "location" => "Adventure Park"];
            $itinerary[] = ["day" => $i, "time" => "08:00 PM", "activity" => "Dinner", "location" => "Restaurant"];
        }
    }

    $response = [
        "inputs" => $data,
        "costs" => [
            "transport" => $transport_cost,
            "hotel" => $hotel_cost,
            "food" => $food_cost,
            "entry_fees" => $entry_fees,
            "guide" => $guide_cost,
            "emergency_buffer" => $emergency_buffer,
            "total_estimated_cost" => $total_cost,
            "budget_status" => ($total_cost <= $budget) ? "Within Budget" : "Exceeds Budget"
        ],
        "suggestions" => [
            "transport" => $selected_transport,
            "hotel" => $selected_hotel
        ],
        "itinerary" => $itinerary
    ];

    http_response_code(200);
    echo json_encode($response);

} else {
    http_response_code(400);
    echo json_encode(array("error" => "Incomplete data. Provide from, to, days, persons, budget."));
}
?>
