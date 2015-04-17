<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/15/15
 * Time: 8:48 PM
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

    $connection = encryptDB();
    $db = $connection->selectDB("security_project")->selectCollection("user");
    if ($db) {

        $result = $db->find();
        $returnList = array();
        if ($result->count() == 0) {
            $replyMessage['serverReply'] = "No users available";
            $replyMessage['userListCount'] = $result->count();
            echo json_encode($replyMessage);
        } else {
            foreach ($result as $document) {
                array_push($returnList, ["username" => decrypt($document[encrypt("username", $key)], $key)]);
            }
//            var_dump($returnList);
            $replyMessage['serverReply'] = "success";
            $replyMessage['userList'] =json_encode($returnList);
            $replyMessage['userListCount'] =$result->count();
            $replyMessage['username'] =$_POST;
            echo json_encode($replyMessage);

        }
    }else {
        $replyMessage['serverReply'] = "Error: Unable to connect MongoDB";
        echo json_encode($replyMessage);
    }

    $connection->close(true);
    }catch(Exception $e){
        $replyMessage['serverReply']='Caught exception: '. $e->getMessage()."\n";
        echo json_encode($replyMessage);

    }


?>