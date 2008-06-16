<?php

function m2__m__foo() {
    echo "m2.m.foo";
}

function m1__m__foo() {
    echo "m1.m.foo";
}

function m1__m__bar() {
    m2__m__foo();
}

m1__m__foo();
m1__m__bar();
?>