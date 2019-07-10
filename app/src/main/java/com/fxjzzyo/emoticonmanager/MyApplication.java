package com.fxjzzyo.emoticonmanager;

import android.app.Application;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

/**
 * Created by fanlulin on 2019-07-09.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 绑定 LitePal 数据库框架
        LitePal.initialize(this);
        // 创建数据库、表
//        Connector.getDatabase();
    }
}
