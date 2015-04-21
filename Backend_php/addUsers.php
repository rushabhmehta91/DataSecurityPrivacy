<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/16/15
 * Time: 1:29 AM
 */


require_once("Cipher.php");
require_once("mongoConnection.php");
try {
    $connectionProxy = proxy();
    $dbProxy = $connectionProxy->selectDB("security_project")->selectCollection("keys");
    $table = array("table" => "file");
    $resultKey = $dbProxy->find($table);
    foreach ($resultKey as $doc) {
        $key = $doc["AES_key"];
    }
    $count = $_POST['count'];
    $filename = $_POST['filename'];
    $owner = $_POST['owner'];
    $users = array();

    $connection = encryptDB();
    $db = $connection->selectDB("security_project")->selectCollection("file");
    if ($db) {
        //var_dump($_POST);
        //var_dump($users);
        $x = array(encrypt("filename", $key) => encrypt($filename, $key), encrypt("owner", $key) => encrypt($owner, $key));
        $result = $db->find($x);
        //echo "count:  ".$result->count();
        for ($index = 1; $index <= $count; $index++) {
            $db->update($x, array('$push' => array(encrypt("users", $key) => encrypt($_POST['user' . $index], $key))));
        }
        $replyMessage['serverReply'] = "success";
        echo json_encode($replyMessage);

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