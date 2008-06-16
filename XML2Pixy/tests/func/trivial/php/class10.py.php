<?php

function __init__class10__C1(&$self) {
    $self["__class_name__"] = "class10__C";
    return $self;
}

function __init__class10__B1(&$self) {
    $self["__class_name__"] = "class10__B";
    return $self;
}

function __init__class10__A1(&$self) {
    $self["__class_name__"] = "class10__A";
    return $self;
}

function class10__A__foo1(&$self) {
    echo "A.foo()";
}

function class10__B__bar1(&$self) {
    echo "B.bar()";
}

function class10__C__bar1(&$self) {
    echo "C.bar()";
}

function __method__bar1(&$self) {
    if ($self["__class_name__"] == "class10__C") {
        return class10__C__bar1($self);
    } elseif ($self["__class_name__"] == "class10__B") {
        return class10__B__bar1($self);
    }
}

function class10__C__foo1(&$self) {
    echo "C.foo()";
}

function __method__foo1(&$self) {
    if ($self["__class_name__"] == "class10__C") {
        return class10__C__foo1($self);
    } elseif ($self["__class_name__"] == "class10__A") {
        return class10__A__foo1($self);
    }
}

?>