<?php

function __init__class9__C1(&$self) {
    $self["__class_name__"] = "class9__C";
    return $self;
}

function __init__class9__B1(&$self) {
    $self["__class_name__"] = "class9__B";
    return $self;
}

function __init__class9__A1(&$self) {
    $self["__class_name__"] = "class9__A";
    return $self;
}

function class9__A__foo1(&$self) {
    echo "A.foo()";
}

function __method__foo1(&$self) {
    if ($self["__class_name__"] == "class9__A") {
        return class9__A__foo1($self);
    } elseif ($self["__class_name__"] == "class9__C") {
        return class9__A__foo1($self);
    }
}

function class9__B__bar1(&$self) {
    echo "B.bar()";
}

function __method__bar1(&$self) {
    if ($self["__class_name__"] == "class9__B") {
        return class9__B__bar1($self);
    } elseif ($self["__class_name__"] == "class9__C") {
        return class9__B__bar1($self);
    }
}

?>