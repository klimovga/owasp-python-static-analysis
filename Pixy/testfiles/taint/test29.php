<?php

// call-by-value with arrays:
// make sure the values for the elements are propagated as well

a();

function a() {
    $a1[1] = $GLOBALS['evil'];
    $a1[2] = 1;
    b($a1);
}

function b($bp1) {
    $b2 = $bp1[1];
    $b3 = $bp1[2];
    ~_hotspot0;     // b.b2:T/D, b.b3:U/D
}



?>
