package com.qualcomm.ftcrobotcontroller;

import android.app.Application;
import android.content.Context;

import java.io.File;

public class DynappApplication extends Application {

    public static final String NAME = "dynapp";
    private static Context context;

    public void onCreate(){
        super.onCreate();
        DynappApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return DynappApplication.context;
    }

    public static File getPrivateFileFolder() {
        return getAppContext().getCacheDir();
    }
}