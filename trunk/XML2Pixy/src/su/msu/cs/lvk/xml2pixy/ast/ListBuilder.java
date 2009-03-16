package su.msu.cs.lvk.xml2pixy.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: KlimovGA
 * Date: 15.10.2008
 * Time: 22:00:31
 */
public class ListBuilder<T extends ASTNode> {

    protected List<T> children = new ArrayList<T>();

    public List<T> toList() {
        return children;
    }

    public ListBuilder<T> add(T elem) {
        if (elem != null) {
            children.add(elem);
        }
        return this;
    }

    public ListBuilder<T> add(Collection<? extends T> elems) {
        if (elems != null) {
            children.addAll(elems);
        }
        return this;
    }

    public ListBuilder<T> add(T[] elems) {
        if (elems != null) {
            children.addAll(Arrays.asList(elems));
        }
        return this;
    }


}
