package com.fxjzzyo.emoticonmanager.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by fanlulin on 2019-07-17.
 */
public class BackupTask extends AsyncTask<String, Void, Integer> {

    public static final String COMMAND_BACKUP = "backupDatabase";
    public static final String COMMAND_RESTORE = "restoreDatabase";

    public static final int BACKUP_SUCCESS = 1;
    public static final int RESTORE_SUCCESS = 2;
    public static final int TASK_FAILED = 0;

    private Context mContext;

    public BackupTask(Context context) {
        this.mContext = context;
    }

    /**
     * @param params
     * @return 0: 执行失败 1：备份成功；2：恢复成功
     */
    @Override
    protected Integer doInBackground(String... params) {
        int result = 0;
        // 默认路径是 /data/data/(包名)/databases/EmoticonManager.db
        File dbFile = mContext.getDatabasePath(Constant.DATABASE_NAME);

        // /storage/emulated/0/EmoticonManager/Backup
        File exportDir = new File(FileUtil.getSDcardRootPath(),
                Constant.APPLICATION_NAME + File.separator+Constant.BACKUP_DIR_NAME);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File backup = new File(exportDir, dbFile.getName());
        String command = params[0];
        if (command.equals(COMMAND_BACKUP)) {
            try {
                boolean copyResult = FileUtil.copyFile(dbFile.getAbsolutePath(), backup.getAbsolutePath());
                if (copyResult) {
                    result = BACKUP_SUCCESS;
                } else {
                    result = TASK_FAILED;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = TASK_FAILED;
            }
            return result;
        } else if (command.equals(COMMAND_RESTORE)) {
            try {
                boolean copyResult = FileUtil.copyFile(backup.getAbsolutePath(), dbFile.getAbsolutePath());
                if (copyResult) {
                    result = RESTORE_SUCCESS;
                } else {
                    result = TASK_FAILED;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = TASK_FAILED;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        switch (integer) {
            case BACKUP_SUCCESS:
                mBackupResultListener.backupSuccess(BACKUP_SUCCESS);
                break;

            case RESTORE_SUCCESS:
                mBackupResultListener.backupSuccess(RESTORE_SUCCESS);
                break;
            case TASK_FAILED:
                mBackupResultListener.backupFailed(TASK_FAILED);
                break;
            default:
                break;
        }

    }

    public interface IBackupResultListener {
        void backupSuccess(int type);

        void backupFailed(int type);
    }

    private IBackupResultListener mBackupResultListener;


    public void setBackupResultListener(IBackupResultListener mBackupResultListener) {
        this.mBackupResultListener = mBackupResultListener;
    }
}
