<?php
function fun6__foo(&$x) {
    if (($x) == (0)) {
        return 1;
    } else {
        $t_arg_0 = ($x) - (1);
        return fun6__foo($t_arg_0);
    }
}
?>