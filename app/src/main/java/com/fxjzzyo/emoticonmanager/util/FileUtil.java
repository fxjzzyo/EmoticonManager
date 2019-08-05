package com.fxjzzyo.emoticonmanager.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by fanlulin on 2019-07-08.
 */
public class FileUtil {

    private static volatile FileUtil mInstance;
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private static final String DEFAULT_SUCCESS_TXT = "success";
    private static final String DEFAULT_FAILED_TXT = "failed";

    private Context mContext;
    private FileOperateCallback mFileOperateCallback;

    public static final String TAG = "FileUtil";

    public static FileUtil getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FileUtil.class) {
                if (mInstance == null) {
                    mInstance = new FileUtil(context);
                }
            }
        }
        return mInstance;
    }

    private FileUtil(Context context) {
        this.mContext = context;
    }

    /**
     * 文件操作的回调接口
     */
    public interface FileOperateCallback {
        void onSuccess(String result);

        void onFailed(String error);
    }

    public void setFileOperateCallback(FileOperateCallback mFileOperateCallback) {
        this.mFileOperateCallback = mFileOperateCallback;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mFileOperateCallback != null) {
                if (msg.what == SUCCESS) {
                    Log.d("tagg", "-msg---success-----");
                    Bundle data = msg.getData();
                    if (data != null) {
                        Log.d("tagg", "---------" + data);
                        mFileOperateCallback.onSuccess(data.getString("result", DEFAULT_SUCCESS_TXT));
                    } else {
                        mFileOperateCallback.onSuccess(DEFAULT_SUCCESS_TXT);
                    }
                }
                if (msg.what == FAILED) {
                    mFileOperateCallback.onFailed(msg.obj.toString());
                }
            }
        }
    };


    /**
     * 获取 sd 卡根目录的路径
     * eg:/storage/emulated/0
     *
     * @return
     */
    public static String getSDcardRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取应用相关的sd卡目录路径
     * /storage/emulated/0/Android/data/包名/files
     * 如果应用卸载了，那么它也没了
     *
     * @param context
     * @return
     */
    public static String getFileRootPath(Context context) {
        return context.getExternalFilesDir(null).getAbsolutePath();
    }

    /**
     * 创建目录
     *
     * @param path 目录的绝对路径
     * @return 创建成功则返回true
     */
    public static boolean createFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }

    /**
     * 创建文件
     *
     * @param path     文件所在目录的目录名
     * @param fileName 文件名
     * @return 文件新建成功则返回true
     */
    public static boolean createFile(String path, String fileName) {
        File file = new File(path + File.separator + fileName);
        if (file.exists()) {
            return false;
        } else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 文件所在的绝对路径
     * @return 删除成功则返回true
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    /**
     * 判断某个文件是否存在
     *
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 删除一个目录（可以是非空目录）
     *
     * @param dir 目录绝对路径
     */
    public static boolean deleteDirection(File dir) {
        if (dir == null || !dir.exists() || dir.isFile()) {
            return false;
        }

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                deleteDirection(file);//递归
            }
        }
        dir.delete();
        return true;
    }

    public FileUtil deleteDirectionInThread(File dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (deleteDirection(dir)) {
                    handler.obtainMessage(SUCCESS).sendToTarget();
                } else {
                    handler.obtainMessage(FAILED).sendToTarget();
                }
            }
        }).start();
        return this;
    }

    /**
     * 拷贝一个文件到另一个文件
     *
     * @param srcPath
     * @param destDirPath
     * @return
     * @throws IOException
     */
    public static boolean copyFile(String srcPath, String destDirPath) {
        try {
            FileChannel inChannel = new FileInputStream(new File(srcPath)).getChannel();
            FileChannel outChannel = new FileOutputStream(new File(destDirPath)).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public FileUtil copyFileInThread(String srcPath, String destDirPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (copyFile(srcPath, destDirPath)) {
                    handler.obtainMessage(SUCCESS).sendToTarget();
                } else {
                    handler.obtainMessage(FAILED).sendToTarget();
                }
            }
        }).start();
        return this;
    }

    /**
     * 将文件转化为 byte[]
     *
     * @param src
     * @return
     */
    private byte[] getImgByteData(String src) {
        File file = new File(src);
        int byteSize = 1024;
        byte[] datas = new byte[byteSize];
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(byteSize);
            int len = 0;
            while ((len = inputStream.read(datas)) > 0) {
                baos.write(datas, 0, len);
            }
            baos.flush();
            inputStream.close();
            baos.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 拷贝文件
     *
     * @param srcPath     绝对路径
     * @param destDirPath 目标文件所在目录的路径
     * @return int true拷贝成功 0:拷贝失败；1：拷贝成功；2：图片已存在
     */
    public static int copyImageFile(String srcPath, String destDirPath) {
        File srcFile = new File(srcPath); // 源文件
        if (!srcFile.exists()) {
            Log.d(TAG, "源文件不存在");
            return 0;
        }
        // 获取待复制文件的文件名
        String fileName = srcPath.substring(srcPath.lastIndexOf(File.separator));

        if (!createFolder(destDirPath)) {
            Log.d(TAG, "创建目标文件目录失败");
            return 0;
        }

        String destPath = destDirPath + fileName;
        if (destPath.equals(srcPath)) {
            Log.d(TAG, "源文件路径和目标文件路径重复");
            return 2;
        }
        File destFile = new File(destPath); // 目标文件
        if (destFile.exists() && destFile.isFile()) {
            Log.d(TAG, "该路径下已经有一个同名文件");
            return 2;
        }

        try {
            FileInputStream fis = new FileInputStream(srcPath);
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int c;
            while ((c = fis.read(buf)) != -1) {
                fos.write(buf, 0, c);
            }
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 压缩并拷贝文件
     *
     * @param srcPath     绝对路径
     * @param destDirPath 目标文件所在目录的路径
     * @return int true拷贝成功 0:拷贝失败；1：拷贝成功；2：图片已存在
     */
    public int compressAndcopyImageFile(String srcPath, String destDirPath) {
        File srcFile = new File(srcPath); // 源文件
        if (!srcFile.exists()) {
            Log.d(TAG, "源文件不存在");
            return 0;
        }
        // 获取待复制文件的文件名
        String fileName = srcPath.substring(srcPath.lastIndexOf(File.separator));

        if (!createFolder(destDirPath)) {
            Log.d(TAG, "创建目标文件目录失败");
            return 0;
        }

        String destPath = destDirPath + fileName;
        if (destPath.equals(srcPath)) {
            Log.d(TAG, "源文件路径和目标文件路径重复");
            return 2;
        }
        File destFile = new File(destPath); // 目标文件
        if (destFile.exists() && destFile.isFile()) {
            Log.d(TAG, "该路径下已经有一个同名文件");
            return 2;
        }

        // 压缩控制
        ImageCompressUtil imageCompressUtil = new ImageCompressUtil();
        byte[] compressBytes = imageCompressUtil.compressImageDataWithSize(mContext,
                getImgByteData(srcPath), Constant.CONTENT_LENGTH_LIMIT);

        try {
            FileOutputStream fos = new FileOutputStream(destFile);
            fos.write(compressBytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    /**
     * 在子线程中拷贝图片
     *
     * @param srcPath
     * @param destDirPath
     * @return
     */
    public FileUtil copyImageFileInThread(String srcPath, String destDirPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = compressAndcopyImageFile(srcPath, destDirPath);
                Bundle bundle = new Bundle();
                bundle.putString("result", result + "");
                Message msg = new Message();
                msg.what = SUCCESS;
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }).start();
        return this;
    }


    /**
     * 获取某个路径下的文件列表
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static File[] getFileList(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                return files;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 保存文件
     *
     * @param bm
     * @param fileDirPath
     * @param fileName
     * @throws IOException
     */
    public static void saveBitmapToFile(Bitmap bm, String fileDirPath, String fileName) {
        File dirFile = new File(fileDirPath);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(fileDirPath + File.separator + fileName);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 子线程中保存图片到本地
     *
     * @param bm
     * @param fileDirPath
     * @param fileName
     * @return
     */
    public FileUtil saveBitmapToFileInThread(Bitmap bm, String fileDirPath, String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveBitmapToFile(bm, fileDirPath, fileName);
                handler.obtainMessage(SUCCESS).sendToTarget();
            }
        }).start();
        return this;
    }

    /**
     * 获取文件的大小
     *
     * @param var1
     * @return
     */
    public static int getFileSize(String var1) {
        if (var1 != null && var1.length() != 0) {
            File var2;
            return !(var2 = new File(var1)).exists() ? 0 : (int) var2.length();
        } else {
            return 0;
        }
    }

}
