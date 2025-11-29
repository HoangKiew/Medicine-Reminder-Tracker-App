<?php
if ($_SERVER["REQUEST_METHOD"] !== "POST") respond(["error" => "Method not allowed"], 405);

if (!isset($input["logId"], $input["minutes"])) {
    respond(["error" => "Missing fields"], 400);
}

$stmt = $pdo->prepare("
    UPDATE LogEntry
    SET 
        scheduledTime = DATE_ADD(scheduledTime, INTERVAL :mins MINUTE),
        status = 'Pending'
    WHERE logId = :log
");

$ok = $stmt->execute([
    ":mins" => $input["minutes"],
    ":log"  => $input["logId"]
]);

$rows = $stmt->rowCount();

$newTime = null;
if ($rows > 0) {
    $q = $pdo->prepare("
        SELECT DATE_FORMAT(scheduledTime, '%Y-%m-%d %H:%i:%s') AS scheduledTime 
        FROM LogEntry 
        WHERE logId = ?
    ");
    $q->execute([$input["logId"]]);
    $r = $q->fetch(PDO::FETCH_ASSOC);
    $newTime = $r['scheduledTime'] ?? null;
}

respond([
    "success" => (bool)$ok,
    "affectedRows" => $rows,
    "logId" => $input["logId"],
    "newScheduledTime" => $newTime,
    "statusReset" => "Pending"
]);