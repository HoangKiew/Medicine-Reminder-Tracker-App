<?php
use Google\Auth\OAuth2;

function sendFCM($token, $title, $body, $logId) {
    $file = __DIR__ . "/../fcm-service-account.json";

    if (!file_exists($file)) {
        error_log("FCM: service account missing!");
        return false;
    }

    $sa = json_decode(file_get_contents($file), true);
    if (!$sa) {
        error_log("FCM: cannot decode service account JSON");
        return false;
    }

    $oauth = new OAuth2([
        'audience' => $sa['token_uri'],
        'issuer' => $sa['client_email'],
        'signingAlgorithm' => 'RS256',
        'signingKey' => $sa['private_key'],
        'tokenCredentialUri' => $sa['token_uri'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging'
    ]);

    $tokenData = $oauth->fetchAuthToken();
    $accessToken = $tokenData['access_token'] ?? null;

    if (!$accessToken) {
        error_log("FCM: cannot get access token");
        return false;
    }

    $url = "https://fcm.googleapis.com/v1/projects/{$sa['project_id']}/messages:send";

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
        error_log("FCM CURL ERR: " . $err);
        return false;
    }

    error_log("FCM RAW: " . $response);
    return true;
}