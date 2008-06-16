<?php

// a VERY simple demo file for getting started;
// see doc/readme.txt for usage instructions;
// for more complex demos, take a look into the "testfiles" folder

$getstarted__a = "hi";
$getstarted__b = $_GET["evil"];

echo $getstarted__a;    // this one is OK
echo $getstarted__b;    // XSS vulnerability

?>