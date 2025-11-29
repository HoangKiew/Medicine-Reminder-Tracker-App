<?php
header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") exit();

require_once "config.php";
require_once "vendor/autoload.php";
require_once "helpers/response.php";

use Google\Auth\OAuth2;

$action = $_GET["action"] ?? "";

// Đọc input
$raw = file_get_contents("php://input");
$input = json_decode($raw, true);
if (empty($input) && !empty($_POST)) {
    $input = $_POST;
}
$input = $input ?? [];

// Route các action
switch ($action) {
    case "getMedicines":       require "actions/getMedicines.php"; break;
    case "getScheduleByDate":  require "actions/getScheduleByDate.php"; break;
    case "markTaken":          require "actions/markTaken.php"; break;
    case "addMedicine":        require "actions/addMedicine.php"; break;
    case "deleteMedicine":     require "actions/deleteMedicine.php"; break;
    case "saveToken":          require "actions/saveToken.php"; break;
    case "checkReminder":      require "actions/checkReminder.php"; break;
    case "getLogDetail":       require "actions/getLogDetail.php"; break;
    case "markMissed":         require "actions/markMissed.php"; break;
    case "markLater":          require "actions/markLater.php"; break;
    default:
        respond(["error" => "Unknown action"], 400);
}