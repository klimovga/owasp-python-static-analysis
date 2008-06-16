package su.msu.cs.lvk.xml2pixy.types;

import su.msu.cs.lvk.xml2pixy.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: Panther
 * Date: 17.01.2008
 * Time: 1:03:23
 */

/**
 * Dummy Type class. Need to rewrite!
 * Grammar:
 * <pre>
 *  type ::= "None" | "Any" | var | primitive | sequence | set
 *      | dict | join | class | instance | func
 *      | "iter" | "file" | "module"
 *  var ::= "'" идентификатор
 *  primitive ::= "bool" | "int" | "float" | "long" | "complex"
 *  sequence ::= "str" | "unicode" | list
 *      | tuple | "buffer" | "xrange"
 *  list ::= "[" type "]
 *  tuple :: = "(" type+ ")"
 *  set ::= ("set" | "frozenset") "[" type "]"
 *  dict ::= "{" type ":" type "}"
 *  func ::= "func" type ("," type)* "->" type
 *  class ::= "<" "class" идентификатор ">"
 *  instance ::= идентификатор
 *  join ::= type "|" type
 * </pre>
 */
public class Type {

    private String type;

    public Type(String type) {
        if (Utils.isBlank(type)) {
            throw new IllegalArgumentException();
        }

        setType(Utils.trimToNull(type));

        /* TODO: correspond grammar */
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }
}
