<?php
function __init__class7__A1(&$self) {
    $self["__class_name__"] = "class7__A";
    return $self;
}

function class7__A__c1(&$self) {
    return null;
}

function __method__c1(&$self) {
    if ($self["__class_name__"] == "class7__A") {
        return class7__A__c1($self);
    }
}

$t_arg_0 = array();
$class7__a = __init__class7__A1($t_arg_0);
$t_arg_1 = array();
$class7__a["b"] = __init__class7__A1($t_arg_1);
$class7__x = __method__c1($class7__a["b"]);
?>