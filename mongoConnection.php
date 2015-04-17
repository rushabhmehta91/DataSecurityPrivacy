<?php
/**
 * Created by PhpStorm.
 * User: rushabhmehta91
 * Date: 4/12/15
 * Time: 8:23 PM
 */

function encryptDB(){
//    return new MongoClient("mongodb://rushabh:mehta@localhost:27017/security_project");
//    return new MongoClient("mongodb://rushabh:mehta@192.168.1.107:27017/security_project");
      return new MongoClient("mongodb://secure_user:A_v!0956UOvb@192.168.206.165:27017/security_project");
}
function proxy(){
    return new MongoClient("mongodb://secure_user:A_v!0956UOvb@localhost:27017/security_project");
}


?>