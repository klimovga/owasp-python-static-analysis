package su.msu.cs.lvk.xml2pixy;

import at.ac.tuwien.infosys.www.phpparser.ParseTree;
import at.ac.tuwien.infosys.www.pixy.conversion.ProgramConverter;

import java.io.File;
import java.io.IOException;

/**
 * Created at: [16.10.2007] 14:27:26
 *
 * @author gklimov
 */

/**
 * Just like pixy Program converter but with .py.xml parser instead of php.
 */
public class PixyConverter extends ProgramConverter {

    public PixyConverter(boolean specialNodes, boolean useAliasAnalysis) {
        super(specialNodes, useAliasAnalysis);
    }


    public ParseTree parse(String fileName) {
        if (isPhpFile(fileName)) {
            return super.parse(fileName);
        }

        try {
            fileName = (new File(fileName)).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return new ParseTree(Utils.transformPython(fileName));

    }

    public static boolean isPhpFile(String filename) {
        return filename.endsWith(".php");
    }
}
