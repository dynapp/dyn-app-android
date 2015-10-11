package com.qualcomm.ftcrobotcontroller.jarloading;

import android.util.Log;

import com.qualcomm.ftcrobotcontroller.DynappApplication;

import java.io.File;
import java.util.List;
import com.android.dx.cf.iface.ParseException;
import com.android.dx.command.Main;
import com.qualcomm.robotcore.util.RobotLog;

/**
 * The {@code DalvikCompiler} class contains static methods for compiling standard java
 * jar files to Dalvik-compatible jar files. This allows the VM to search and instantiate
 * classes in the jar later.
 *
 * @author Zach Ohara
 */
public class DalvikCompiler {

    /**
     * The sub-directory of the application's private data folder that is used to cache
     * the Dalvik-compiled jar files. The actual directory doesn't matter at all, as long
     * as the directory is not used for anything else.
     */
    private static final String OUTPUT_TO = "/" + DynappApplication.NAME + "/compiled";

    /**
     * Compiles the given list of jar files to Dalvik-compatible jar files, and stores the compiled
     * jar files in a cache folder. As every jar file is compiled, its {@code File} in the list is
     * changed to reflect its new location. Files that cannot be opened are logged to Logcat.
     * <br>
     * The files in {@code jarList} must all be .jar files as a prerequisite.
     *
     * @param jarList the list of jar files to compile.
     */
    public static void convertJars(List<File> jarList) {
        Log.i(DynappApplication.NAME, "Converting Jar List...");
        for (int i = 0; i < jarList.size(); i++) {
            File f = jarList.get(i);
            try {
                Log.i(DynappApplication.NAME, "Converting: " + f.getName());
                Main.main(new String[]{"--dex", "--output=" + getOutputFile(f).getAbsolutePath(), f.getAbsolutePath()});
                jarList.set(i, getOutputFile(f));
                Log.i(DynappApplication.NAME, "Converted: " + f.getName());
            } catch (ParseException ex) {
                Log.i(DynappApplication.NAME, f.getName() + " was probably generated using the wrong compiler!");
                Log.i(DynappApplication.NAME, "Make sure to use Java 1.6");
            }
        }
    }

    /**
     * This will clear all previously compiled Jars and regenerate the directory if it does not exist
     */
    public static void cleanCompileCache() {
        Log.i(DynappApplication.NAME, "Clearing compile Cache...");
        File output =  getOutputDirectory();
        if (output.exists()) {
            output.delete();
        }
        output.mkdirs();
    }

    /**
     * Generates a file path for the compiled and cached jar from the given jar file.
     *
     * @param inputFile the jar file to generate a cache file path for.
     * @return the cache file path for the given jar.
     */
    private static File getOutputFile(File inputFile) {
        String compiledName = "compiled-" + inputFile.getName().replace(" ", "-");
        return new File(getOutputDirectory(), compiledName);
    }

    /**
     * Obtains the outputdirectory files will be compiled to
     *
     * @return Output directory path
     */
    private static File getOutputDirectory() {
        return new File(DynappApplication.getPrivateFileFolder(), OUTPUT_TO);
    }

    /**
     * Removes all the non-jar files from the given list of {@code File} objects.
     *
     * @param fileList a list of file objects.
     * @return the list of file objects, but with all non-jar files removed.
     */
    public static List<File> getJarList(List<File> fileList) {
        Log.i(DynappApplication.NAME, "Reading Jar List...");
        for (int i = fileList.size() - 1; i >= 0; i--) {
            if (!fileList.get(i).getName().endsWith(".jar")) {
                fileList.remove(i);
            }
        }
        return fileList;
    }

}