package su.msu.cs.lvk.xml2pixy.postproc;

/**
 * Manages temporary variables.
 *
 * @author ikonv
 */
public class TemporaryVars {
    private int varCounter;

    public String createArgVar() {
        return "$t_arg_" + varCounter++;
    }

    public String createExprVar() {
        return "$t_expr_" + varCounter++;
    }

    public String createArrayVar() {
        return "$t_arr_" + varCounter++;
    }

    public String createFunctionVar() {
        return "$t_funvar_" + varCounter++;
    }

    public String createFunctionArg() {
        return "$t_funarg_" + varCounter++;
    }

    public String createFunctionRes() {
        return "$t_funres_" + varCounter++;
    }
}
