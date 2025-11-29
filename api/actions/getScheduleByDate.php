<?php
if (!isset($_GET["userId"], $_GET["date"]))
    respond(["error" => "Missing userId or date"], 400);

$stmt = $pdo->prepare("
    SELECT 
        l.logId,
        l.medicineId,
        DATE_FORMAT(l.scheduledTime, '%H:%i') AS time,
        DATE_FORMAT(l.actualTime, '%Y-%m-%d %H:%i:%s') AS actualTime,
        l.status,
        m.name,
        m.medicineType,
        m.dosage
    FROM LogEntry l
    JOIN Medicine m ON m.medicineId = l.medicineId
    WHERE m.userId = :uid
      AND m.isActive = 1
      AND DATE(l.scheduledTime) = :day
    ORDER BY l.scheduledTime
");

$stmt->execute([
    ":uid" => $_GET["userId"],
    ":day" => $_GET["date"]
]);

respond(["schedule" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);