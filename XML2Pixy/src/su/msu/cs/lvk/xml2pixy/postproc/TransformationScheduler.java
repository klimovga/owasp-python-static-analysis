package su.msu.cs.lvk.xml2pixy.postproc;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;

import java.util.List;
import java.util.ArrayList;

/**
 * @author ikonv
 */
public class TransformationScheduler {
    private List<List<NodeTransformer>> passes = new ArrayList<List<NodeTransformer>>();
    private List<NodeTransformer> transformers; // used as a temporary variable

    public void addTranformer(NodeTransformer transformer, int pass) {
        while (passes.size() <= pass) {
            passes.add(new ArrayList<NodeTransformer>());
        }

        passes.get(pass).add(transformer);
    }

    public void findAndTransform(ParseNode root) {
        for (List<NodeTransformer> t : passes) {
            transformers = t;

            List<TransformationPoint> points = new ArrayList<TransformationPoint>();
            findTransformationPoints(root, points);

            for (TransformationPoint point : points) {
                point.getTransformer().apply(point.getTransformationParam());
            }
        }
    }

    private void findTransformationPoints(ParseNode node, List<TransformationPoint> points) {
        for (NodeTransformer t : transformers) {
            Object param = t.createArgIfApplicable(node);
            if (param != null) {
                points.add(new TransformationPoint(t, param));
            }
        }

        for (int i = 0; i < node.getNumChildren(); ++i) {
            findTransformationPoints(node.getChild(i), points);
        }
    }

}
