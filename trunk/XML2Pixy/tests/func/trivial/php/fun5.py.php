<?php
function fun5__foo(&$x) {
    $k = $x;
}

function fun5__bar(&$x) {
    $k = fun5__foo($x);
}
?>