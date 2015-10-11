package com.qualcomm.ftcrobotcontroller.jarloading;

import android.os.Environment;
import android.util.Log;

import com.qualcomm.ftcrobotcontroller.DynappApplication;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.RobotLog;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * The {@code OpModeClassLoader} class is responsible for loading class files into instantiated
 * objects.
 *
 * @author Zach Ohara
 */
public class OpModeClassLoader {

    private static ClassLoader classLoader;

    public static final String FILE_LOCATION = "FIRST";

    public static List<Class<? extends OpMode>> loadJars(List<File> fileList) {
        URL[] jarurls = getJarURLs(fileList);
        classLoader = getClassLoader(jarurls);
        Thread.currentThread().setContextClassLoader(classLoader);
        List<Class<? extends OpMode>> opModeList = new ArrayList<Class<? extends OpMode>>();
        for (File jarfile : fileList) {
            if (jarfile.getName().endsWith(".jar")) {
                try {
                    loadJarOpmodes(jarfile, opModeList);
                } catch (Exception e) {
                    Log.wtf(DynappApplication.NAME, "Failed to load: " + jarfile.getName(), e);
                }
            }
        }
        return opModeList;
    }

    private static ClassLoader getClassLoader(URL[] jarurls) {
        Log.i(DynappApplication.NAME, "Getting Classloader...");
        String pathString = getDelimitedPathString(jarurls);
        File cacheFile = new File(DynappApplication.getPrivateFileFolder(), "/" + DynappApplication.NAME + "/");
        cacheFile.mkdirs();
        String cacheDir = cacheFile.toString();
        ClassLoader parentLoader = OpModeClassLoader.class.getClassLoader();
        return new DexClassLoader(pathString, cacheDir, null, parentLoader);
    }

    /**
     * Returns a single string that contains the value of toString() for every object given
     * in the array, delimited by {@code java.io.File#pathSeperator}
     *
     * @param arr the array of objects.
     * @param <T> the type of the array. This doesn't really matter at all.
     * @return the concatenation of o.toString() for every object o in arr.
     */
    private static <T> String getDelimitedPathString(T[] arr) {
        String result = "";
        for (T obj : arr){
            result += File.pathSeparator;
            result += obj.toString();
        }
        return result.substring(1);
    }

    /**
     * Loads the classes contained in a given jar file. The jar file must have already been converted
     * to a dalvik-compatible form.
     *
     * @param jarfile the file to load classes from.
     * @throws IOException if an IOException is thrown by the underlying class loading system.
     */
    private static void loadJarOpmodes(File jarfile, List<Class<? extends OpMode>> list) throws IOException {
        Log.i(DynappApplication.NAME, "Loading...: " + jarfile.getName());
        File cache = new File(DynappApplication.getPrivateFileFolder(), "/" + DynappApplication.NAME + "/temp");
        DexFile jarobj = DexFile.loadDex(jarfile.getAbsolutePath(), cache.getAbsolutePath(), 0);
        Enumeration<String> jarentries = jarobj.entries();
        while (jarentries.hasMoreElements()) {
            String entry = jarentries.nextElement();
            loadIfOpmode(entry, list);
        }
        jarobj.close();
        Log.i(DynappApplication.NAME, "Loaded: " + jarfile.getName());
    }

    /**
     * Attempts to load a class with the given fully-qualified name. Any exception thrown from
     * within this method will be caught and handled (either ignored or logged).
     * <br>
     * The class will be loaded by the {@code classLoader} class variable. If the loaded class is
     * a valid, instantiable {@code OpMode}, it will be added to the {@code opModeList} class
     * variable.
     *
     * @param classname the fully-qualified name of the class to be resolved.
     */
    private static void loadIfOpmode(String classname, List<Class<? extends OpMode>> list) {
        try {
            Class<?> c = classLoader.loadClass(classname);
            Object instance = c.newInstance();
            if (instance instanceof OpMode) {
                Log.i(DynappApplication.NAME, "Found " + classname + " as an op mode!");
                list.add(((OpMode)instance).getClass());
            }
        } catch (Throwable ex) {
            //Log.e(LOG_TAG, "Exception while loading " + entryname + ": ");//, ex)
            //Log.e(LOG_TAG, ex.getMessage());// ;
        }
    }

    private static URL[] getJarURLs(List<File> fileList) {
        Log.i(DynappApplication.NAME, "Getting Jar List...");
        List<URL> jarList = new ArrayList<URL>();
        for (File f : fileList) {
            if (f.isFile() && f.getName().endsWith(".jar")) {
                try {
                    jarList.add(f.getAbsoluteFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    Log.wtf(DynappApplication.NAME, e);
                }
            }
        }
        return jarList.toArray(new URL[jarList.size()]);
    }

    /**
     * Gets a list of all the files contained within the app's directory, usually "sdcard/FIRST/"
     *
     * @return a list of all files in the apps directory.
     */
    public static List<File> getFileSet() {
        List<File> fileList = new ArrayList<File>();
        getFilesInDirectory(getTargetDirectory(), fileList);
        return fileList;
    }

    /**
     * Recursively browses the given directory and builds a list that contains every file
     * in the given directory.
     *
     * @param current the directory to look for files in.
     * @param fileList the list of all files in the given directory. This list should be
     * empty when
     */
    private static void getFilesInDirectory(File current, List<File> fileList) {
        for (File f : current.listFiles()) {
            if (f.isFile()) {
                fileList.add(f);
            } else if (f.isDirectory()) {
                getFilesInDirectory(f, fileList);
            }
        }
    }

    /**
     * Gets the {@code File} object for the directory that should be searched for jar files.
     *
     * @return the {@code File} to search.
     */
    private static File getTargetDirectory() {
        File sdcard = Environment.getExternalStorageDirectory();
        return new File(sdcard, FILE_LOCATION);
    }

}