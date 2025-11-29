<?php
if (!isset($_GET["medicineId"]))
    respond(["error" => "Missing medicineId"], 400);

$stmt = $pdo->prepare("
    UPDATE Medicine SET isActive = 0 WHERE medicineId = ?
");

respond(["success" => $stmt->execute([$_GET["medicineId"]])]);