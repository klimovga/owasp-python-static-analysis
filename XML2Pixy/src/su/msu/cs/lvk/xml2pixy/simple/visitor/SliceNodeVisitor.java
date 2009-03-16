package su.msu.cs.lvk.xml2pixy.simple.visitor;

import org.apache.log4j.Logger;
import su.msu.cs.lvk.xml2pixy.ast.python.CallFuncNode;
import su.msu.cs.lvk.xml2pixy.ast.python.NameNode;
import su.msu.cs.lvk.xml2pixy.ast.python.PythonNode;
import su.msu.cs.lvk.xml2pixy.ast.python.SliceNode;
import su.msu.cs.lvk.xml2pixy.simple.ProcessingUtils;

import java.util.Arrays;

/**
 * User: klimov
 * Date: 18.01.2009
 */
public class SliceNodeVisitor extends PythonNodeVisitor {

    private static final Logger logger = Logger.getLogger(SliceNodeVisitor.class);

    public void visit(PythonNode node) {

        SliceNode slice = (SliceNode) node;

        if (ProcessingUtils.APPLY_FLAG.equalsIgnoreCase(slice.getFlags())) {

            PythonNode callSliceFunction = new CallFuncNode(
                    new NameNode("analysis_slice_list").copyLocation(slice),
                    Arrays.asList(slice.getExpr(), slice.getLower(), slice.getUpper()),
                    null,
                    null
            );
            
            slice.getParent().replace(slice, callSliceFunction);

        } else {
            logger.warn("WARNING: Unsupported kind of Slice found (line: "
                    + node.getLocation() + ")");
        }

    }
}
