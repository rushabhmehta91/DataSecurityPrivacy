<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/12/15
 * Time: 7:00 PM
 * ReferredLink : http://php.net/manual/en/function.mcrypt-encrypt.php
 */

function encrypt($input, $key)
{
    return base64_encode(mcrypt_encrypt(MCRYPT_RIJNDAEL_256, $key, $input, MCRYPT_MODE_ECB));
}

function decrypt($encrypted_string, $key)
{
    return trim(mcrypt_decrypt(MCRYPT_RIJNDAEL_256, $key, base64_decode($encrypted_string), MCRYPT_MODE_ECB));
}

/* Random Key Generation Function */
function generateKey($len)
{
    $result = "";
    $chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    $charArray = str_split($chars);
    for ($i = 0; $i < $len; $i++) {
        $randItem = array_rand($charArray);
        $result .= "" . $charArray[$randItem];
    }
    return $result;
}


?>