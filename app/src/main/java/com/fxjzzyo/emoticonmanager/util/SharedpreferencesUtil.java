package com.fxjzzyo.emoticonmanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by fanlulin on 2019-07-17.
 */
public class SharedpreferencesUtil {

    public static final String KEY_FIRST_LANUCH = "KeyFirstLanuch";

    public static void saveBoolean(Context context, String key, boolean value){
        SharedPreferences spf = context.getSharedPreferences(Constant.APPLICATION_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public static boolean getBoolean(Context context,String key){
        SharedPreferences spf = context.getSharedPreferences(Constant.APPLICATION_NAME,Context.MODE_PRIVATE);
        return spf.getBoolean(key,true);
    }

}
