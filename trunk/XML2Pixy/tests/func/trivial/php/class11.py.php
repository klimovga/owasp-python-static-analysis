<?php
function __init__class11__A1(&$self) {
    $self["__class_name__"] = "class11__A";
    return $self;
}

function class11__A__foo1(&$self) {
}

function __method__foo1(&$self) {
    if ($self["__class_name__"] == "class11__A") {
        return class11__A__foo1($self);
    }
}

$t_arg_0 = null;
class11__A__foo1($t_arg_0);
?>