<?php
function module1__module2__foo() {
    return main__bar();
}

function main__bar() {
    return null;
}

function main__m() {
    module1__module2__foo();
}
?>