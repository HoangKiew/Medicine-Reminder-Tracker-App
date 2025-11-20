<?php
header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS");

require_once "config.php";
require_once "vendor/autoload.php";

use Google\Auth\OAuth2;

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") exit();

$action = $_GET["action"] ?? "";

// Try to decode JSON body first (Retrofit usually sends JSON)
$raw = file_get_contents("php://input");
$input  = json_decode($raw, true);

// Fallback: if JSON decode failed or empty, use $_POST (form-data)
if (empty($input) && !empty($_POST)) {
    $input = $_POST;
}
$input = $input ?? []; // ensure array

/* ============================================================
   RESPONSE HELPER
   ============================================================ */
function respond($data, $code = 200) {
    http_response_code($code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit;
}

/* ============================================================
   GET MEDICINES
   ============================================================ */
if ($action === "getMedicines") {

    if (!isset($_GET["userId"]))
        respond(["error" => "userId required"], 400);

    $stmt = $pdo->prepare("
        SELECT * FROM Medicine
        WHERE userId = :uid AND isActive = 1
    ");
    $stmt->execute([":uid" => $_GET["userId"]]);

    respond(["medicines" => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
}


/* ============================================================
   GET SCHEDULE BY DATE
   ============================================================ */
if ($action === "getScheduleByDate") {

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
}


/* ============================================================
   MARK TAKEN
   Accepts JSON or form with { "logId": "..." }
   ============================================================ */
if ($action === "markTaken" && $_SERVER["REQUEST_METHOD"] === "POST") {

    if (!isset($input["logId"]) || empty($input["logId"]))
        respond(["error" => "Missing logId"], 400);

    $stmt = $pdo->prepare("
        UPDATE LogEntry
        SET status = 'Taken',
            actualTime = NOW()
        WHERE logId = ?
    ");

    $ok = $stmt->execute([$input["logId"]]);
    $rows = $stmt->rowCount();

    respond([
        "success" => (bool)$ok,
        "affectedRows" => $rows,
        "logId" => $input["logId"],
        "status" => $rows > 0 ? "Taken" : null
    ]);
}


/* ============================================================
   ADD MEDICINE
   ============================================================ */
if ($action === "addMedicine" && $_SERVER["REQUEST_METHOD"] === "POST") {

    if (!isset($input["userId"], $input["name"], $input["scheduleTimes"]))
        respond(["error" => "Missing fields"], 400);

    try {
        $pdo->beginTransaction();

        $medId = newId("M");
        $timesCsv = implode(",", $input["scheduleTimes"]);

        /* Insert Medicine */
        $stmt = $pdo->prepare("
            INSERT INTO Medicine (
                medicineId, userId, name, medicineType, dosage,
                timesPerDay, specificTimes, notes, isActive
            )
            VALUES (:id, :uid, :name, :type, :dos, :tpd, :st, :nt, 1)
        ");

        $stmt->execute([
            ":id" => $medId,
            ":uid" => $input["userId"],
            ":name" => $input["name"],
            ":type" => $input["medicineType"] ?? null,
            ":dos" => $input["dosage"] ?? null,
            ":tpd" => count($input["scheduleTimes"]),
            ":st"  => $timesCsv,
            ":nt"  => $input["notes"] ?? null
        ]);

        /* Insert Schedule (trigger sẽ tạo LogEntry) */
        $stmt2 = $pdo->prepare("
            INSERT INTO Schedule (scheduleId, medicineId, scheduleDate, specificTime)
            VALUES (:sid, :mid, :date, :time)
        ");

        $start = $input["startDate"];
        $freq  = $input["frequency"];

        if ($freq === "Once") {

            foreach ($input["scheduleTimes"] as $t) {
                $t24 = date("H:i", strtotime($t)) . ":00";

                $stmt2->execute([
                    ":sid" => newId("S"),
                    ":mid" => $medId,
                    ":date" => $start,
                    ":time" => $t24
                ]);
            }

        } else {
            // 30 ngày
            for ($i = 0; $i < 30; $i++) {

                $d = date("Y-m-d", strtotime("$start +$i day"));

                foreach ($input["scheduleTimes"] as $t) {
                    $t24 = date("H:i", strtotime($t)) . ":00";

                    $stmt2->execute([
                        ":sid" => newId("S"),
                        ":mid" => $medId,
                        ":date" => $d,
                        ":time" => $t24
                    ]);
                }
            }
        }

        $pdo->commit();
        respond(["success" => true, "medicineId" => $medId]);

    } catch (Exception $e) {
        $pdo->rollBack();
        respond(["error" => $e->getMessage()], 500);
    }
}


/* ============================================================
   DELETE MEDICINE
   ============================================================ */
if ($action === "deleteMedicine") {

    if (!isset($_GET["medicineId"]))
        respond(["error" => "Missing medicineId"], 400);

    $stmt = $pdo->prepare("
        UPDATE Medicine SET isActive = 0 WHERE medicineId = ?
    ");

    respond(["success" => $stmt->execute([$_GET["medicineId"]])]);
}


/* ============================================================
   SAVE TOKEN
   ============================================================ */
if ($action === "saveToken" && $_SERVER["REQUEST_METHOD"] === "POST") {

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
}


/* ============================================================
   CHECK REMINDER (FIXED)
   ============================================================ */
if ($action === "checkReminder") {
    file_put_contents(__DIR__."/task_log.txt", date("Y-m-d H:i:s")." - checkReminder CALLED\n", FILE_APPEND);
    file_put_contents(__DIR__."/task_log.txt", "UserId = ".$_GET["userId"]."\n", FILE_APPEND);

    if (!isset($_GET["userId"]))
        respond(["error" => "Missing userId"], 400);

    // Lấy thời gian hiện tại
    $now_minute = date("H:i");

    error_log("CHECK REMINDER – NOW = $now_minute");

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


    $stmt->execute([
        ":uid" => $_GET["userId"],
    ]);

    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    error_log("MATCHED = " . json_encode($items));

    if (empty($items)) respond(["message" => "No reminder now"]);

    // Lấy token
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
}


/* ============================================================
   SEND FCM V1
   ============================================================ */
function sendFCM($token, $title, $body, $logId) {
    $file = __DIR__ . "/fcm-service-account.json";

    if (!file_exists($file)) {
        error_log("FCM: service account missing!");
        return false;
    }

    // Load file JSON
    $sa = json_decode(file_get_contents($file), true);

    if (!$sa) {
        error_log("FCM: cannot decode service account JSON");
        return false;
    }

    // Tạo OAuth2 đúng chuẩn Google API
    $oauth = new OAuth2([
        'audience' => $sa['token_uri'],
        'issuer' => $sa['client_email'],
        'signingAlgorithm' => 'RS256',
        'signingKey' => $sa['private_key'],
        'tokenCredentialUri' => $sa['token_uri'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging'
    ]);

    // Lấy access token
    $tokenData = $oauth->fetchAuthToken();
    $accessToken = $tokenData['access_token'] ?? null;

    if (!$accessToken) {
        error_log("FCM: cannot get access token");
        return false;
    }

    // URL theo project_id
    $url = "https://fcm.googleapis.com/v1/projects/{$sa['project_id']}/messages:send";

    /* DATA ONLY PAYLOAD (có logId) */
    $data = [
        "message" => [
            "token" => $token,
            "data" => [
                "title" => $title,
                "body"  => $body,
                "logId" => $logId
            ],
            "android" => [
                "priority" => "high"
            ]
        ]
    ];

    // cURL gửi FCM
    $ch = curl_init();
    curl_setopt_array($ch, [
        CURLOPT_URL => $url,
        CURLOPT_POST => true,
        CURLOPT_HTTPHEADER => [
            "Authorization: Bearer $accessToken",
            "Content-Type: application/json"
        ],
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POSTFIELDS => json_encode($data),
        CURLOPT_TIMEOUT => 10
    ]);

    $response = curl_exec($ch);
    $err = curl_error($ch);
    curl_close($ch);

    if ($err) {
        error_log("🔥 FCM CURL ERR: " . $err);
        return false;
    }

    error_log("🔥 FCM RAW: " . $response);

    return true;
}

/* ============================================================
   GET LOG DETAIL (LẤY THÔNG TIN THUỐC TỪ logId)
   ============================================================ */
if ($action === "getLogDetail") {

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
}

/* ============================================================
   MARK MISSED (Người dùng nhấn "Bỏ qua")
   ============================================================ */
if ($action === "markMissed" && $_SERVER["REQUEST_METHOD"] === "POST") {

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
}


/* ============================================================
   MARK LATER (Dời lịch X phút)
   ============================================================ */
if ($action === "markLater" && $_SERVER["REQUEST_METHOD"] === "POST") {

    if (!isset($input["logId"], $input["minutes"])) {
        respond(["error" => "Missing fields"], 400);
    }

    // 1️⃣ Dời lịch + đặt lại trạng thái Pending
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

    // 2️⃣ Lấy giờ mới để trả về cho client
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
}

/* ============================================================
   DEFAULT
   ============================================================ */
respond(["error" => "Unknown action"], 400);
