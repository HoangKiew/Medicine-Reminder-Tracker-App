<?php
if ($_SERVER["REQUEST_METHOD"] !== "POST") respond(["error" => "Method not allowed"], 405);

if (!isset($input["logId"]) || empty($input["logId"]))
    respond(["error" => "Missing logId"], 400);

$stmt = $pdo->prepare("
    UPDATE LogEntry
    SET status = 'Missed',
        actualTime = NOW()
    WHERE logId = ?
");

$ok = $stmt->execute([$input["logId"]]);
$rows = $stmt->rowCount();

respond([
    "success" => (bool)$ok,
    "affectedRows" => $rows,
    "logId" => $input["logId"],
    "status" => $rows > 0 ? "Missed" : null
]);