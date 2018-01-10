package com.example.kmorfin.samplecameratest;

import android.os.Environment;

import java.io.File;

/**
 * Created by kmorfin on 1/10/18.
 */

public final class BaseAlbumDirFactory {
    private static final String CAMERA_DIR = "/dcim/";

    public File getAlbumStorageDir(String albumName){
        return new File(Environment.getExternalStorageDirectory() + CAMERA_DIR + albumName);
    }
}
