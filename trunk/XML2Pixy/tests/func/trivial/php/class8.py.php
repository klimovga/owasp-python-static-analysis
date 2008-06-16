<?php

function __init__class8__C1(&$self) {
    $self["__class_name__"] = "class8__C";
    return $self;
}

function __init__class8__B1(&$self) {
    $self["__class_name__"] = "class8__B";
    return $self;
}
function __init__class8__A1(&$self) {
    $self["__class_name__"] = "class8__A";
    return $self;
}

function class8__A__foo1(&$self) {
    echo "A.foo()";
}


function class8__B__bar1(&$self) {
    echo "B.bar()";
}

function __method__bar1(&$self) {
    if ($self["__class_name__"] == "class8__B") {
        return class8__B__bar1($self);
    } elseif ($self["__class_name__"] == "class8__C") {
        return class8__B__bar1($self);
    }
}

function class8__C__foo1(&$self) {
    echo "C.foo()";
}

function __method__foo1(&$self) {
    if ($self["__class_name__"] == "class8__C") {
        return class8__C__foo1($self);
    } elseif ($self["__class_name__"] == "class8__A") {
        return class8__A__foo1($self);
    } elseif ($self["__class_name__"] == "class8__B") {
        return class8__A__foo1($self);
    }
}

?>