<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/12/15
 * Time: 5:01 PM
 */
//error_reporting(E_ALL);
require_once('Zend/Filter/File/Encrypt.php');
require_once("Cipher.php");
require_once("mongoConnection.php");


try {
    $flag = 0;
    if (!empty($_FILES)) {
        $headers = apache_request_headers();
        $owner = $headers['owner'];
        $filename = basename($_FILES['uploaded_file']['name']);

        $connectionProxy = proxy();
        $dbProxy = $connectionProxy->selectDB("security_project")->selectCollection("keys");
        $table = array("table" => "file");
        $resultKey = $dbProxy->find($table);
        foreach ($resultKey as $doc) {
            $key = $doc["AES_key"];
        }

        $connectionProxy->close(true);
        $encFilename = str_replace("/", "", encrypt("enc." . $owner . "_" . $filename, $key));
        $file_path = "uploads/";
        $file_path = $file_path . $filename;

        $size = $_FILES['uploaded_file']['size'];
        if (($size < 10000000)) {

            //Check if the file with the same name is already exists on the server
            if (!file_exists("uploads/" . $encFilename)) {
                //Attempt to move the uploaded file to it's new place
                if (move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {

                    $flag = 1;
                } else {
                    $replyMessage['serverReply'] = "Error in uploading file";
                    echo json_encode($replyMessage);
                }
            } else {
                $replyMessage['serverReply'] = "Error: File " . $_FILES["uploaded_file"]["name"] . " already exists";
                echo json_encode($replyMessage);
            }
        } else {
            $replyMessage['serverReply'] = "Error: File size too large for upload";
            echo json_encode($replyMessage);
        }
    } else {

        $replyMessage['serverReply'] = "Error: No file uploaded - " . $_FILES['uploaded_file']['name'] . "\n\n" . "Error: " . $_FILES['uploaded_file']['error'];
        echo json_encode($replyMessage);

    }

//echo $flag;

    /*	ENCRYPT FILE - Using Zend Libraries */
    /*	Generate valid initialization vector (iv) and encryption key for rijndael-256 encryption */
    if ($flag == 1) {
        $docKey = md5(generateKey(32));
//echo "key: ".$docKey."\n\n";
        $vector = md5(generateKey(32));
//echo "vector; ".$vector."\n\n";

        /*  Set various encryption options. */
        $options = array(
            // Encryption type - Openssl or Mcrypt
            'adapter' => 'mcrypt',
            // Initialization vector
            'vector' => $vector,
            // Encryption algorithm
            'algorithm' => 'rijndael-256',
            // Encryption key
            'key' => $docKey  // generate this for each upload
        );
//print_r($options);

        /* Initialize the library and pass the options */
        $encrypt = new Zend_Filter_File_Encrypt($options);
        /* Generate a random vector */
//$encrypt->setVector();
//$vector = $encrypt->getVector();
//echo "\n\nvector: ".$vector."\n\n";


        /*
           Set output filename, where the encrypted file will be stored.
           If we omit this, the encrypted file will overwrite the original file.
        */
        $encrypt->setFilename('uploads/' . $encFilename);  // set for each upload

        /* Now encrypt a file */
        if ($encrypt->filter('uploads/' . $filename) == null) {
            $replyMessage['serverReply'] = "Error: File cannot be encrypted - " . $_FILES['uploaded_file']['name'] . "\n\n";
            echo json_encode($replyMessage);
        } else {
            $docCode = generateKey(16);
            unlink($file_path);
            $connection = encryptDB();// connect
            $db = $connection->selectDB("security_project")->selectCollection("file");
            if ($db) {
                $users = array(encrypt($owner, $key));
                $encryptDetails = array(encrypt("vector", $key) => encrypt($vector, $key), encrypt("key", $key) => encrypt($docKey, $key), encrypt("docCode", $key) => encrypt($docCode, $key), encrypt("filename", $key) => encrypt($filename, $key), encrypt("size", $key) => encrypt($size, $key), encrypt("createdOn", $key) => encrypt(date("Y/m/d H:i:s", strtotime("now")), $key), encrypt("owner", $key) => encrypt($owner, $key), encrypt("users", $key) => $users, encrypt("path", $key) => encrypt($file_path, $key), encrypt("last_downloadedBy", $key) => encrypt(null, $key), encrypt("last_downloadedOn", $key) => encrypt(null, $key));
                $db->insert($encryptDetails);
                $replyMessage['serverReply'] = "success";
                echo json_encode($replyMessage);

            } else {
                $replyMessage['serverReply'] = "Error: Unable to connect MongoDB";
                echo json_encode($replyMessage);
            }

            $connection->close(true);


        }
    }
} catch (Exception $e) {
    $replyMessage['serverReply'] = 'Caught exception: ' . $e->getMessage();
    echo json_encode($replyMessage);

}

?>