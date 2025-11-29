<?php
require_once __DIR__ . "/../helpers/fcm.php";

file_put_contents(__DIR__."/../task_log.txt", date("Y-m-d H:i:s")." - checkReminder CALLED\n", FILE_APPEND);
file_put_contents(__DIR__."/../task_log.txt", "UserId = ".$_GET["userId"]."\n", FILE_APPEND);

if (!isset($_GET["userId"]))
    respond(["error" => "Missing userId"], 400);

$stmt = $pdo->prepare("
    SELECT l.logId, m.name, l.scheduledTime
    FROM LogEntry l
    JOIN Medicine m ON m.medicineId = l.medicineId
    WHERE m.userId = :uid
      AND l.status = 'Pending'
      AND DATE(l.scheduledTime) = CURDATE()
      AND l.scheduledTime <= NOW()
      AND l.scheduledTime >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
");

$stmt->execute([":uid" => $_GET["userId"]]);
$items = $stmt->fetchAll(PDO::FETCH_ASSOC);

error_log("MATCHED = " . json_encode($items));

if (empty($items)) respond(["message" => "No reminder now"]);

$tk = $pdo->prepare("SELECT fcmToken FROM UserToken WHERE userId = ?");
$tk->execute([$_GET["userId"]]);
$token = $tk->fetchColumn();

if (!$token) respond(["error" => "User has no token"]);

$sent = 0;
$update = $pdo->prepare("UPDATE LogEntry SET status = 'Sent' WHERE logId = ?");

foreach ($items as $i) {
    $time = date("H:i", strtotime($i["scheduledTime"]));
    if (sendFCM($token, "Đến giờ uống thuốc!", "Bạn cần uống {$i['name']} lúc $time", $i["logId"])) {
        $update->execute([$i["logId"]]);
        $sent++;
    }
}

respond(["success" => true, "sent" => $sent]);