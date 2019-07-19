package com.fxjzzyo.emoticonmanager.util;

/**
 * Created by fanlulin on 2019-07-08.
 */
public class Constant {
    public static final String APPLICATION_NAME = "EmoticonManager"; // 应用名
    public static final String IMAGE_DIR_NAME = "EmoticonManagerImages"; // 图片文件夹名
    public static final String BACKUP_DIR_NAME = "Backup"; // 数据库备份文件夹名
    public static final String DATABASE_NAME = "EmoticonManager.db"; // 数据库名
    public static final String APP_ID = "wxf2fed5d006f9acdc";

    public static volatile boolean isNetAvaiable = true;// 网络是否可用

    // 标识每次进入程序（为了不toast网络情况）
    public static boolean isFirstIn = true;

    // 安装后第一次启动该应用
    public static boolean isFirstLanch = true;

    // 数据库是否改动过，如果有改动则退出程序时备份数据库
    public static boolean isDatabaseMotified = false;

    public static int PAGE_COUNT = 20;// 一次从数据库加载的数据个数
}
