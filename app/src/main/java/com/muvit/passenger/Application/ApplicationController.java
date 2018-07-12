package com.muvit.passenger.Application;

import android.app.Application;
import android.graphics.Bitmap;

/**
 * Created by nct96 on 29-10-02015.
 */
public class ApplicationController extends Application {

    private static final String TAG = ApplicationController.class.getSimpleName();
    public Bitmap cropped = null;
    public Bitmap img = null;
    public static boolean isOnline = true;
}
