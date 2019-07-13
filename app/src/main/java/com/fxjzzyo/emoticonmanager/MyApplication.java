package com.fxjzzyo.emoticonmanager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.fxjzzyo.emoticonmanager.receiver.NetWorkChangReceiver;
import com.fxjzzyo.emoticonmanager.util.Constant;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import static com.fxjzzyo.emoticonmanager.util.Constant.APP_ID;

/**
 * Created by fanlulin on 2019-07-09.
 */
public class MyApplication extends Application {

    private NetWorkChangReceiver netWorkChangReceiver;

    // IWXAPI 是第三方app和微信通信的openApi接口
    public static IWXAPI api;

    private void regToWx() {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);

        // 将应用的appId注册到微信
        api.registerApp(APP_ID);

        //建议动态监听微信启动广播进行注册到微信
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // 将该app注册到微信
                api.registerApp(APP_ID);
            }
        }, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));

    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 绑定 LitePal 数据库框架
        LitePal.initialize(this);
        // 注册微信分享
        regToWx();
        //注册网络状态监听广播
        registerNetStateReceiver();
    }

    private void registerNetStateReceiver() {
        netWorkChangReceiver = new NetWorkChangReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //解绑
        if (netWorkChangReceiver!=null) {
            unregisterReceiver(netWorkChangReceiver);
        }
    }
}
