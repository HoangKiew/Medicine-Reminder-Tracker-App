<?php
if (!isset($_GET["logId"]))
    respond(["error" => "Missing logId"], 400);

$stmt = $pdo->prepare("
    SELECT 
        l.logId,
        l.medicineId,
        m.name,
        m.dosage,
        m.medicineType,
        DATE_FORMAT(l.scheduledTime, '%Y-%m-%d %H:%i:%s') AS scheduledTime
    FROM LogEntry l
    JOIN Medicine m ON m.medicineId = l.medicineId
    WHERE l.logId = ?
    LIMIT 1
");

$stmt->execute([$_GET["logId"]]);
$row = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$row)
    respond(["error" => "Not found"], 404);

respond($row);