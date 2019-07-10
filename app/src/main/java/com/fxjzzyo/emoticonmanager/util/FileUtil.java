package com.fxjzzyo.emoticonmanager.util;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by fanlulin on 2019-07-08.
 */
public class FileUtil {

    public static final String TAG = "FileUtil";

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
     * 在 sd 卡下创建一个文件
     *
     * @param filePath
     */
    public static void createFileInSDcard(String filePath) {

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
            return file.mkdir();
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
     * @param path     文件所在的绝对路径
     * @param fileName 文件名
     * @return 删除成功则返回true
     */
    public static boolean deleteFile(String path, String fileName) {
        File file = new File(path + File.separator + fileName);
        return file.exists() && file.delete();
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

    /**
     * 拷贝文件
     *
     * @param srcPath     绝对路径
     * @param destDirPath 目标文件所在目录的路径
     * @return boolean true拷贝成功
     */
    public static boolean copyFile(String srcPath, String destDirPath) {
        boolean flag = false;
        File srcFile = new File(srcPath); // 源文件
        if (!srcFile.exists()) {
            Log.d(TAG, "源文件不存在");
            return false;
        }
        // 获取待复制文件的文件名
        String fileName = srcPath.substring(srcPath.lastIndexOf(File.separator));

        if (!createFolder(destDirPath)) {
            Log.d(TAG, "创建目标文件目录失败");
            return false;
        }

        String destPath = destDirPath + fileName;
        if (destPath.equals(srcPath)) {
            Log.d(TAG, "源文件路径和目标文件路径重复");
            return true;
        }
        File destFile = new File(destPath); // 目标文件
        if (destFile.exists() && destFile.isFile()) {
            Log.d(TAG, "该路径下已经有一个同名文件");
            return true;
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
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
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

}
