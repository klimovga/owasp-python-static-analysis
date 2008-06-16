<?php
function __init__class3__A1(&$self) {
    $self["__class_name__"] = "class3__A";
    return $self;
}

function class3__A__meth2(&$self, &$str) {
        echo $self;
        echo $str;
}

function __method__meth2(&$self, &$str) {
    if ($self["__class_name__"] == "class3__A") {
        return class3__A__meth2($self, $str);
    }
}
?>