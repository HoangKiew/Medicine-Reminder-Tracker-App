<?php
if ($_SERVER["REQUEST_METHOD"] !== "POST") respond(["error" => "Method not allowed"], 405);

if (!isset($input["userId"], $input["token"]))
    respond(["error" => "Missing userId or token"], 400);

$stmt = $pdo->prepare("
    INSERT INTO UserToken (userId, fcmToken)
    VALUES (:uid, :tk)
    ON DUPLICATE KEY UPDATE fcmToken = VALUES(fcmToken)
");

$stmt->execute([
    ":uid" => $input["userId"],
    ":tk"  => $input["token"]
]);

respond(["success" => true]);