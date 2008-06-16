<?php
function module1__module2__foo() {
    return null;
}

function main__bar() {
    return module1__module2__foo();
}
?>