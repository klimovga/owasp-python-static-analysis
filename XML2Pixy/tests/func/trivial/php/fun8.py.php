<?php
function analysis_slice_list(&$arr, $start, $end ) {
    $new_arr = array() ;
    for ($i = $start ; $i < $end ; $i++) {
        array_push($new_arr, $arr[$i]);
    }
    return $new_arr;
}
function fun8__foo($a = 1, $b = array(), $c = array("a" => 1), $d = null) {
}
?>