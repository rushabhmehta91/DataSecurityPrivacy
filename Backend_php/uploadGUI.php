<?php
require_once('Cipher.php');
echo date("Y/m/d H:i:s", strtotime("now"));

$docKey = md5(generateKey(32));
//echo "key: ".$docKey."\n\n";
$vector = md5(generateKey(32));
//echo "vector; ".$vector."\n\n";
//echo "</br>"."$docKey"."</br>";
//echo "$vector";
$encrypted_string = encrypt("hi how are u?", $docKey);


$deccrypt = decrypt($encrypted_string, $docKey);
echo $deccrypt;
?>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>Upload Testing</title>
</head>

<body>
<!--<form enctype="multipart/form-data" action="processUpload_zend.php" method="POST">-->
<!--    <input type="hidden" name="MAX_FILE_SIZE" value="100000000000" />-->
<!--        Choose a file to upload: <input name="uploaded_file" type="file" />-->
<!--    <br />-->
<!--    <input type="submit" value="Upload File" />-->
<!--</form>-->
<form action="download.php" method="POST">
    <input type="hidden" name="filename" value="view.pdf"/>
    <input type="hidden" name="owner" value="shah.sohil123@gmail.com"/>

    <input type="submit" value="Upload File"/>
</form>
</body>
</html>