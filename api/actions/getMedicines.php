<?php
if (!isset($_GET["userId"]))
    respond(["error" => "userId required"], 400);

$stmt = $pdo->prepare("
    SELECT * FROM Medicine
    WHERE userId = :uid AND isActive = 1
");
$stmt->execute([":uid" => $_GET["userId"]]);

respond(["medicines" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);