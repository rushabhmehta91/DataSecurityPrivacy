<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/12/15
 * Time: 12:43 PM
 */
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
    $username = encrypt($_POST['username'], $key);
    $password = encrypt($_POST['password'], $key);
//    var_dump($_POST);
//    $username = encrypt($_GET['username'], $key);
//    $password = encrypt($_GET['password'], $key);
//    var_dump($_GET);
    $connection = encryptDB();
    $db = $connection->selectDB("security_project")->selectCollection("user");
    if ($db) {
        $userArray = array(encrypt('username', $key) => $username, encrypt('password', $key) => $password);
        $result = $db->find($userArray);
        if ($result->count() == 0) {
            $replyMessage['serverReply'] = "Error: Invalid Username and Password";
            echo json_encode($replyMessage);
        } else {
            if ($result->count() == 1) {
                $replyMessage['serverReply'] = "success";
                echo json_encode($replyMessage);
            } else {
                $replyMessage['serverReply'] = "Error: multiple user with same username and password";
                echo json_encode($replyMessage);
            }
        }


    } else {
        $replyMessage['serverReply'] = "Error: Unable to connect MongoDB";
        echo json_encode($replyMessage);
    }

    $connection->close(true);

} catch (Exception $e) {
    $replyMessage['serverReply'] = 'Caught exception: ' . $e->getMessage() . "\n";
    echo json_encode($replyMessage);

}

?>