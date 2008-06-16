package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;

/**
 * Checks, whether ParseNode should be transformed, and performs transformation.
 *
 * @author ikonv
 */
public interface NodeTransformer {
    Object createArgIfApplicable(ParseNode node);
    void apply(Object arg);
}
