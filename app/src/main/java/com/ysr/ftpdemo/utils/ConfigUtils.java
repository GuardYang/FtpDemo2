package com.ysr.ftpdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by Administrator on 2016/8/11.
 */
public class ConfigUtils {
    public static String ftpIp = "172.19.0.3";
    public static int ftpPort = 21;
    public static String ftpUser = "ftpserver";
    public static String ftpPassWord = "123qwe!@#";
    public static ConfigEntity loadftpConfig(Context cx) {
        ConfigEntity entity = new ConfigEntity();
        SharedPreferences share = cx.getSharedPreferences("ConfigUtils", Context.MODE_WORLD_WRITEABLE);
        entity.ftpIp = share.getString("ftpIp", ftpIp);
        entity.ftpPort = share.getInt("ftpPort", ftpPort);
        entity.ftpUser = share.getString("ftpUser", ftpUser);
        entity.ftpPassWord = share.getString("ftpPassWord", ftpPassWord);
        return entity;
    }
    public static void  saveftpConfig(Context cx,ConfigEntity entity) {
        SharedPreferences share = cx.getSharedPreferences("ConfigUtils", Context.MODE_WORLD_WRITEABLE);
        Editor editor= share.edit();
        editor.putString("ftpIp", entity.ftpIp);
        editor.putInt("ftpPort", entity.ftpPort);
        editor.putString("ftpUser", entity.ftpUser);
        editor.putString("ftpPassWord", entity.ftpPassWord);
        editor.commit();
    }
}
