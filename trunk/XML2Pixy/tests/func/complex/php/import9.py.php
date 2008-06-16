<?php

function __init__module__A1(&$self) {
    $self["__class_name__"] = "module__A";
    return $self;
}

function module__A__meth1(&$self) {
}

function __method__meth1(&$self) {
    if ($self["__class_name__"] == "module__A") {
        return module__A__meth1($self);
    }
}

$t_arg_0 = array();
$module__variable = __init__module__A1($t_arg_0);
__method__meth1($module__variable);
?>