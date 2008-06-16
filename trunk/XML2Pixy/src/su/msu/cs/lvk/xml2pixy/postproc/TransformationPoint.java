package su.msu.cs.lvk.xml2pixy.postproc;

/**
 * @author ikonv
 */
public class TransformationPoint {
    private NodeTransformer transformer;
    private Object transformationParam;

    public TransformationPoint(NodeTransformer transformer, Object transformationArgument) {
        this.transformer = transformer;
        this.transformationParam = transformationArgument;
    }


    public NodeTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(NodeTransformer transformer) {
        this.transformer = transformer;
    }

    public Object getTransformationParam() {
        return transformationParam;
    }

    public void setTransformationParam(Object transformationParam) {
        this.transformationParam = transformationParam;
    }
}
