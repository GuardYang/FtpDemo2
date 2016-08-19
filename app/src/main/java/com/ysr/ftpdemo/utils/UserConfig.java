package com.ysr.ftpdemo.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by Administrator on 2016/8/19.
 */
public class UserConfig {

    public static int widthPixels = -1;
    public static int heightPixels = -1;
    public static int getWidthPixels(Activity activity) {
        if (-1 == widthPixels) {
            DisplayMetrics dm = getDisplayMetrics(activity);
            widthPixels = dm.widthPixels;
        }
        return widthPixels;
    }

    public static int getHeightPixels(Activity activity) {
        if (-1 == heightPixels) {
            DisplayMetrics dm = getDisplayMetrics(activity);
            heightPixels = dm.heightPixels;
        }
        return heightPixels;
    }
    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPixels = dm.widthPixels;
        heightPixels = dm.heightPixels;
        return dm;
    }
}
