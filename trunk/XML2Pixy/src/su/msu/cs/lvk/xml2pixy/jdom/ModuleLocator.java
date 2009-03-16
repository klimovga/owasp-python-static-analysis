package su.msu.cs.lvk.xml2pixy.jdom;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Locates module for import according to python import rules.
 * <p/>
 * User: konnov
 * Date: 28.01.2008
 * Time: 16:55:00
 */
public class ModuleLocator {
    public File locateImport(File importer, String currentModule, String moduleToImport) throws ModuleNotFoundException {
        String dir = getParentDir(importer);
        boolean xml = !"py".equalsIgnoreCase(StringUtils.substringAfterLast(importer.getAbsolutePath(), "."));

        // try to find module module
        String modulePath = moduleToImport.replaceAll("\\.", "/");
        File moduleFile = locateImport(dir + "/" + modulePath, xml);
        if (moduleFile != null) {
            return moduleFile;
        }

        // try to find global module
        int countDots = countDots(currentModule);
        while (countDots-- > 0) {
            dir = getParentDir(new File(dir));
        }

        moduleFile = locateImport(dir + "/" + modulePath, xml);
        if (moduleFile != null) {
            return moduleFile;
        }

        throw new ModuleNotFoundException("Module " + moduleToImport + " imported from " + importer + " not found");
    }

    /**
     *
     * @param path - path to find path/__init__.py.xml or path.py.xml file
     * @param xml flag if xml files are used. False if direct python sources are used.
     * @return found file or null, if no file was found
     */
    public File locateImport(String path, boolean xml) {
        File moduleFile = new File(path);
        String ext = xml ? ".py.xml" : ".py";
        if (moduleFile.exists() && moduleFile.isDirectory()) {
            File tmp = new File(path + "/__init__" + ext);
            if (tmp.exists()) {
                moduleFile = tmp;
            }
        }
        if (!moduleFile.exists()) {
            moduleFile = new File(path + ext);
        }
        return moduleFile.exists() ? moduleFile : null;
    }


    protected int countDots(String str) {
        int i = 0;
        for (char c : str.toCharArray()) {
            if (c == '.') i++;
        }
        return i;
    }

    protected String getParentDir(File file) {
        String dir;
        try {
            dir = file.getCanonicalFile().getParentFile().getCanonicalFile().getPath();
        } catch (IOException e) {
            dir = file.getParent();
        }

        return dir;
    }
}
