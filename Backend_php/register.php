<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/11/15
 * Time: 10:00 PM
 */
error_reporting(E_ALL);
require_once("Cipher.php");
require_once("mongoConnection.php");

try {

    $connectionProxy = proxy();
    $dbProxy = $connectionProxy->selectDB("security_project")->selectCollection("keys");
    $table = array("table" => "user");
    $resultKey = $dbProxy->find($table);
    foreach ($resultKey as $doc) {
        $key = $doc["AES_key"];
    }
    $connectionProxy->close(true);

    $username = encrypt($_POST['username'], $key);
    $password = encrypt($_POST['password'], $key);
    $dob = encrypt($_POST['dob'], $key);
    $name = encrypt($_POST['name'], $key);

    $connection = encryptDB();
    $db = $connection->selectDB("security_project")->selectCollection("user");
    if ($db) {
        $checkUsers = array('username' => $username);
        $result = $db->find($checkUsers);
//        echo "herehi m";
        if ($result->count() == 0) {
            //echo "here if block";
            $userArray = array(encrypt('username', $key) => $username, encrypt('password', $key) => $password, encrypt('dob', $key) => $dob, encrypt('name', $key) => $name);
            $db->insert($userArray);
            $replyMessage['serverReply'] = "success";
            echo json_encode($replyMessage);
        } else {
            $replyMessage['serverReply'] = "Error: Username already exist";
            echo json_encode($replyMessage);
        }
    } else {
        $replyMessage['serverReply'] = "Error: Unable to connect database";
        echo json_encode($replyMessage);
    }
} catch (Exception $e) {
    $replyMessage['serverReply'] = 'Caught exception: ' . $e->getMessage() . "\n";
    echo json_encode($replyMessage);
    $connection->close(true);

}
$connection->close(true);


?>