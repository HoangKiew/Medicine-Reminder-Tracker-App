<?php
if ($_SERVER["REQUEST_METHOD"] !== "POST") respond(["error" => "Method not allowed"], 405);

if (!isset($input["userId"], $input["name"], $input["scheduleTimes"]))
    respond(["error" => "Missing fields"], 400);

try {
    $pdo->beginTransaction();

    $medId = newId("M");
    $timesCsv = implode(",", $input["scheduleTimes"]);

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