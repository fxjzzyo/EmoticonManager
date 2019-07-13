package com.fxjzzyo.emoticonmanager.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;

/**
 * Created by fxjzzyo on 2017/7/12.
 */

public class NetUtil {

    private static volatile NetUtil mInstance;

    //使用弱引用持有上下文防止内存泄漏
    private WeakReference<Context> mContext;

    private NetUtil(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    /**
     * 单例模式获取NetUtils
     *
     * @return
     */
    public static NetUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (NetUtil.class) {
                if (mInstance == null) {
                    mInstance = new NetUtil(context);
                }
            }
        }
        return mInstance;
    }


    /**
     * 判断网络是否可用
     *
     * @return
     */
    public boolean isNetworkAvailable() {

        ConnectivityManager cm = (ConnectivityManager) mContext.get()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isAvailable();
            }
        }
        return false;
    }

}
