package com.muvit.passenger.Application;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.graphics.Bitmap;

import com.muvit.passenger.database.AppDatabase;

/**
 * Created by nct96 on 29-10-02015.
 */
public class ApplicationController extends Application {

    private static final String TAG = ApplicationController.class.getSimpleName();
    public Bitmap cropped = null;
    public Bitmap img = null;
    public static boolean isOnline = true;


    public static ApplicationController instance;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .build();
    }

    public static ApplicationController getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }
}
