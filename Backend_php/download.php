<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/11/15
 * Time: 10:00 PM
 */
//error_reporting(E_ALL);
require_once('Zend/Filter/File/Decrypt.php');
require_once("Cipher.php");
require_once("mongoConnection.php");
try {
    $owner = $_POST["owner"];
    $filename = $_POST["filename"];
//echo "</br>";
//echo "</br>";
//    var_dump($_POST);
//echo "</br>";
//echo "</br>";
////
//$owner=$_GET["owner"];
//$filename=$_GET["filename"];

    $connection = encryptDB();// connect
    $db = $connection->selectDB("security_project")->selectCollection("file");
    if ($db) {
        $connectionProxy = proxy();
        $dbProxy = $connectionProxy->selectDB("security_project")->selectCollection("keys");
        $table = array("table" => "file");
        $resultKey = $dbProxy->find($table);
        foreach ($resultKey as $doc) {
            $key = $doc["AES_key"];
        }
        $connectionProxy->close(true);
        $x = array(encrypt("filename", $key) => encrypt($filename, $key), encrypt("owner", $key) => encrypt($owner, $key));
        $resultFile = $db->find($x);
        foreach ($resultFile as $document) {

//       var_dump($document);
            $dec_filename = decrypt($document[encrypt("filename", $key)], $key);
            $dec_vector = decrypt($document[encrypt("vector", $key)], $key);
            $dec_docKey = decrypt($document[encrypt("key", $key)], $key);
            $path = decrypt($document[encrypt('path', $key)], $key);
//}
        }


        $encFilename = str_replace("/", "", encrypt("enc." . $owner . "_" . $filename, $key));
        $decFilename = $dec_filename;
        if (!file_exists('uploads/' . $encFilename)) {
            $replyMessage['serverReply'] = "Error: File $filename not found";
            echo json_encode($replyMessage);
            exit();
        } else {

            /*  Set various encryption options. */
            $options2 = array(
                // Encryption type - Openssl or Mcrypt
                'adapter' => 'mcrypt',
                // Initialization vector
                'vector' => $dec_vector,
                // Encryption algorithm
                'algorithm' => 'rijndael-256',
                // Encryption key
                'key' => $dec_docKey
            );

            /* Initialize the library and pass the options */
            $decrypt = new Zend_Filter_File_Decrypt($options2);
            /* Set output filename, where the decrypted file will be stored. */
            $decrypt->setFilename('uploads/' . $decFilename);
            /* Now decrypt the previously encrypted file */
            $decrypt->filter('uploads/' . $encFilename);


            header('Content-Disposition: inline; filename="' . $decFilename . '"');
            header('Content-Transfer-Encoding: binary');
            header('Accept-Ranges: bytes');
            header('Expires: 0');
            header('Cache-Control: must-revalidate, post-check=0, pre-check=0');
            header('Pragma: public');
            header('Content-Length: ' . filesize('uploads/' . $encFilename));
//            ob_clean();


            @readfile('uploads/' . $decFilename);


            /* Delete the copied decrypted from the temp folder */
            unlink('uploads/' . $decFilename);

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