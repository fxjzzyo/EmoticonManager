package com.fxjzzyo.emoticonmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.fxjzzyo.emoticonmanager.util.Constant;

/**
 * Created by fxjzzyo
 * on date 2019/7/13 0013
 */
public class NetWorkChangReceiver extends BroadcastReceiver {

    /**
     * 获取连接类型
     *
     * @param type
     * @return
     */
    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "网络数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
// 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Constant.isNetAvaiable = true;
                        //每次开启程序不需要toast网络连接成功
                        if (Constant.isFirstIn) {
                            Constant.isFirstIn = false;
                            return;
                        }
                        Toast.makeText(context, getConnectionType(info.getType()) + "已连接", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getConnectionType(info.getType()) + "断开了哦", Toast.LENGTH_SHORT).show();
                    Constant.isNetAvaiable = false;
                }
            }
        }
    }
}
