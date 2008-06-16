<?php
function fun4__foo(&$x) {
    $k = $x;
}

function fun4__bar(&$x) {
    fun4__foo($x);
}
?>