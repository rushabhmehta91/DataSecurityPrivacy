<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/12/15
 * Time: 5:01 PM
 */
require_once("Cipher.php");
require_once("mongoConnection.php");

try{
    $username=$_POST['username'];
    //$username=$_POST['username'];
    //echo $username;

//var_dump(json_encode($_POST));
if($connection = encryptDB()) {
    $db = $connection->selectDB("security_project")->selectCollection("file");
    if ($db) {
        $connectionProxy = proxy();
        $dbProxy = $connectionProxy->selectDB("security_project")->selectCollection("keys");
        $table=array("table" => "file");
        $resultKey=$dbProxy->find($table);
        //echo "hi sohil";
        foreach ($resultKey as $doc){
            $key = $doc["AES_key"];
        }


        $userArray = array(encrypt('users',$key) => encrypt($username,$key));
        $result=$db->find($userArray);
        $returnList=array();
        if($result->count()==0){
            $replyMessage['serverReply'] = "No files available";
            $replyMessage['fileListCount'] =$result->count();
            echo json_encode($replyMessage);
        }else {
            foreach ($result as $document) {
                  array_push($returnList,["filename"=>decrypt($document[encrypt("filename",$key)],$key),"owner" => decrypt($document[encrypt("owner",$key)],$key)]);
            }
            $replyMessage['serverReply'] ="success";
            $replyMessage['fileList'] =json_encode($returnList);
            $replyMessage['fileListCount'] =$result->count();
            $replyMessage['username'] =$_POST;
            echo json_encode($replyMessage);
        }


    } else {
        $replyMessage['serverReply'] = "Error: Unable to connect MongoDB";
        echo json_encode($replyMessage);
    }

    $connection->close(true);
}else{
    $replyMessage['serverReply'] = "Error: Unable to connect MongoDB";
    echo json_encode($replyMessage);
}
}catch(Exception $e){
    $replyMessage['serverReply']='Caught exception: '. $e->getMessage()."\n";
    echo json_encode($replyMessage);

}


?>