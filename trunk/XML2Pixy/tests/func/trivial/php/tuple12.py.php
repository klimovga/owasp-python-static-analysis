<?php
function tuple12__foo(&$x) {
    return ($x) + (1);
}    

$t_arr_0 = array ( ) ;
$t_arg_1 = 1 ;
$t_arr_0 [ 0 ] = tuple12__foo ( $t_arg_1 ) ;
$t_arg_2 = 2 ;
$t_arr_0 [ 1 ] = tuple12__foo ( $t_arg_2 ) ;
list ( $a , $b ) = $t_arr_0 ;
?>