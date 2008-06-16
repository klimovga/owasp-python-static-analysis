<?php
function __init__main__IntegerType1 ( $self ) {
    $self [ "__class_name__" ] = "main__IntegerType" ;
    main__IntegerType____init__1 ( $self ) ;
    return $self ;
}
function __init__main__EnumType1 ( $self ) {
    $self [ "__class_name__" ] = "main__EnumType" ;
    main__EnumType____init__1 ( $self ) ;
    return $self ;
}
function __init__main__Type3 ( $self , $type_id , $type_name ) {
    $self [ "__class_name__" ] = "main__Type" ;
    main__Type____init__3 ( $self , $type_id , $type_name ) ;
    return $self ;
}
function main__Type__declaration_str0 ( ) { }
function main__Type__char_len0 ( ) { }
function main__Type__decode2 ( & $self , & $value ) { }
function main__Type__encode2 ( & $self , & $value ) { }
function main__Type__has_value1 ( & $self ) { }
function __method__has_value1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__Type" ) {
        return main__Type__has_value1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
       return  main__Type__has_value1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__Type__has_value1 ( $self ) ;
    }
}
function main__Type__get_type_name1 ( & $self ) {
    return $self [ "name" ] ;
}
function __method__get_type_name1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__Type" ) {
        return main__Type__get_type_name1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__Type__get_type_name1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__Type__get_type_name1 ( $self ) ;
    }
}
function main__Type__get_type_id1 ( & $self ) {
    return $self [ "id" ] ;
}
function __method__get_type_id1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__Type" ) {
        return main__Type__get_type_id1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__Type__get_type_id1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__Type__get_type_id1 ( $self ) ;
    }
}
function main__Type____init__3 ( & $self , $type_id , $type_name ) {
    $self [ "id" ] = $type_id ;
    $self [ "name" ] = $type_name ;
}

function main__EnumType__declaration_str1 ( & $self ) {
    $i = 1 ;
    $cnt = __builtin____len ( $self [ "_values" ] ) ;
    $str = "enum { " ;
    foreach ( $self [ "_values" ] as $v ) {
        $str += $v ;
        if ( ( $i ) != ( $cnt ) ) {
            $str += ", " ;
        } else {
            $str += "}" ;
        }
        $i += 1 ;
    }
    return $str ;
}
function main__EnumType__char_len1 ( & $self ) {
    if ( $self [ "_is_single_char" ] ) {
        return 1 ;
    } else {
        return $self [ "_dig_len" ] ;
    }
}
function main__EnumType__decode2 ( & $self , & $encoded_val ) {
    if ( ( __builtin____int ( $encoded_val ) ) >= ( __builtin____len ( $self [ "_values" ] ) ) ) {
        echo sprintf("values = %s, index = %d, encoded = %s", array ( __builtin____str ( $self [ "_values" ] ) , __builtin____int ( $encoded_val ) , $encoded_val ) );
    }
    return $self [ "_values" ] [ __builtin____int ( $encoded_val ) ] ;
}
function main__EnumType__encode2 ( & $self , & $value ) {
    if ( $self [ "_is_single_char" ] ) {
        return $value ;
    } else {
        return __method__zfill2 ( __builtin____str ( __method__index2 ( $self [ "_values" ] , $value ) ) , $self [ "_dig_len" ] ) ;
    }
}
function main__EnumType__has_value2 ( & $self , & $value ) {
    return in_array( ( $value ), ( $self [ "_values" ] ) );
}
function main__EnumType__add_value2 ( & $self , & $value ) {
    if ( ! in_array( ( $value ) , ( $self [ "_values" ] ) ) ) { 
        __method__append2 ( $self [ "_values" ] , $value ) ;
    }
    $self [ "_dig_len" ] = main___len_in_digits ( ( __builtin____len ( $self [ "_values" ] ) ) - ( 1 ) ) ;
}
function main__EnumType__get_values1 ( & $self ) {
    return $self [ "_values" ] ;
}
function main__EnumType____init__1 ( & $self ) {
    main__Type____init__3 ( $self , $TYPE_ENUM , "Enum" ) ;
    $self [ "_values" ] = array ( ) ;
    $self [ "_dig_len" ] = 0 ;
    $self [ "_is_single_char" ] = true ;
}
function main__IntegerType__declaration_str1 ( & $self ) {
    return "int" ;
}
function __method__declaration_str1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__declaration_str1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__EnumType__declaration_str1 ( $self ) ;
    }
}
function main__IntegerType__char_len1 ( & $self ) {
    return $self [ "_dig_len" ] ;
}

function __method__char_len1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__char_len1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__EnumType__char_len1 ( $self ) ;
    }
}
function main__IntegerType__decode2 ( & $self , & $encoded_val ) {
    return __builtin____int ( $encoded_val ) ;
}
function __method__decode2 ( & $self , & $encoded_val ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__decode2 ( $self , $encoded_val ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__EnumType__decode2 ( $self , $encoded_val ) ;
    } elseif ( $self [ "__class_name__" ] == "main__Type" ) {
        return main__Type__decode2 ( $self , $encoded_val ) ;
    }
}
function main__IntegerType__encode2 ( & $self , & $value ) {
    return __method__zfill2 ( __builtin____str ( ( __builtin____int ( $value ) ) - ( __method__get_low1 ( $self ) ) ) , $self [ "_dig_len" ] ) ;
}
function __method__encode2 ( & $self , & $value ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__encode2 ( $self , $value ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__EnumType__encode2 ( $self , $value ) ;
    } elseif ( $self [ "__class_name__" ] == "main__Type" ) {
        return main__Type__encode2 ( $self , $value ) ;
    }
}
function main__IntegerType__get_high1 ( & $self ) {
    return $self [ "_range" ] [ 1 ] ;
}
function __method__get_high1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__get_high1 ( $self ) ;
    }
}
function main__IntegerType__get_low1 ( & $self ) {
    return $self [ "_range" ] [ 0 ] ;
}
function __method__get_low1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__get_low1 ( $self ) ;
    }
}
function main__IntegerType__has_value2 ( & $self , & $value ) {
    return ( ( $value ) >= ( __method__get_low1 ( $self ) ) ) && ( ( $value ) <= ( __method__get_high1 ( $self ) ) ) ;
}
function __method__has_value2 ( & $self , & $value ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__has_value2 ( $self , $value ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__EnumType__has_value2 ( $self , $value ) ;
    }
}
function main__IntegerType__add_value2 ( & $self , & $value ) {
    $int_val = __builtin____int ( $value ) ;
    if ( ( $self [ "_range" ] ) == ( null ) ) {
        $self [ "_range" ] = array ( $int_val , $int_val ) ;
    } else {
        $self [ "_range" ] = array ( __builtin____min ( __method__get_low1 ( $self ) , $int_val ) , __builtin____max ( __method__get_high1 ( $self ) , $int_val ) ) ;
    }
    $self [ "_dig_len" ] = main___len_in_digits ( ( ( __method__get_high1 ( $self ) ) - ( __method__get_low1 ( $self ) ) ) + ( 1 ) ) ;
}
function __method__add_value2 ( & $self , & $value ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__add_value2 ( $self , $value ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__EnumType__add_value2 ( $self , $value ) ;
    }
} function main__IntegerType__get_values1 ( & $self ) {
    return __builtin____range ( $self [ "_range" ] [ 0 ] , ( $self [ "_range" ] [ 1 ] ) + ( 1 ) ) ;
} function __method__get_values1 ( & $self ) {
    if ( $self [ "__class_name__" ] == "main__IntegerType" ) {
        return main__IntegerType__get_values1 ( $self ) ;
    } elseif ( $self [ "__class_name__" ] == "main__EnumType" ) {
        return main__EnumType__get_values1 ( $self ) ;
    }
}
function main__IntegerType____init__1 ( & $self ) {
    main__Type____init__3 ( $self , $TYPE_INT , "Integer" ) ;
    $self [ "_range" ] = array ( 0 , - ( 1 ) ) ;
    $self_dig_len = 1 ;
}
function main___len_in_digits ( $value ) {
$l = 1 ;
$upper = 10 ;
while ( ( $value ) >= ( $upper ) ) {
$upper = ( 10 ) * ( $upper ) ;
$l += 1 ; }
return $l ; } $TYPE_ENUM = 1 ; $TYPE_INT = 2 ;
?>