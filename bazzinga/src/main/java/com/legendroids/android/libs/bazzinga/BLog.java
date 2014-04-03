package com.legendroids.android.libs.bazzinga;

import android.util.Log;

/**
 * Created by advantej on 4/2/14.
 */
public class BLog
{
    public static void v(String TAG, String msg)
    {
        if (BuildConfig.DEBUG)
            Log.v(TAG, msg);
    }

    public static void v(String TAG, String msg, Throwable thr)
    {
        if (BuildConfig.DEBUG)
            Log.v(TAG, msg, thr);
    }

    public static void d(String TAG, String msg)
    {
        if (BuildConfig.DEBUG)
            Log.d(TAG, msg);
    }

    public static void d(String TAG, String msg, Throwable thr)
    {
        if (BuildConfig.DEBUG)
            Log.d(TAG, msg, thr);
    }

    public static void i(String TAG, String msg)
    {
        Log.i(TAG, msg);
    }

    public static void i(String TAG, String msg, Throwable thr)
    {
        Log.i(TAG, msg, thr);
    }

    public static void w(String TAG, String msg, Throwable thr)
    {
        Log.w(TAG, msg, thr);
    }

    public static void w(String TAG, String msg)
    {
        Log.w(TAG, msg);
    }


    public static void e(String TAG, String msg)
    {
        Log.e(TAG, msg);
    }

    public static void e(String TAG, String msg, Throwable thr)
    {
        Log.e(TAG, msg, thr);
    }
}
