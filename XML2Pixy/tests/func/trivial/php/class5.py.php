<?php
function __init__class5__A1(&$self) {
    $self["__class_name__"] = "class5__A";
    return $self;
}

function class5__A__b1(&$self) {
    return null;
}

function __method__b1(&$self) {
    if ($self["__class_name__"] == "class5__A") {
        return class5__A__b1($self);
    }
}

$t_arg_0 = array();
$class5__a = __init__class5__A1($t_arg_0);
$class5__x = __method__b1($class5__a);
?>