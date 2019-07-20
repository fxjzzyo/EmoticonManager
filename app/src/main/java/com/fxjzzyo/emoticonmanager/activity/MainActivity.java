package com.fxjzzyo.emoticonmanager.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.fxjzzyo.emoticonmanager.R;
import com.fxjzzyo.emoticonmanager.adapter.EmoticonAdapter;
import com.fxjzzyo.emoticonmanager.bean.EmoticonBean;
import com.fxjzzyo.emoticonmanager.util.BackupTask;
import com.fxjzzyo.emoticonmanager.util.Constant;
import com.fxjzzyo.emoticonmanager.util.FileUtil;
import com.fxjzzyo.emoticonmanager.util.SharedpreferencesUtil;
import com.fxjzzyo.emoticonmanager.util.WXutil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.rcv)
    RecyclerView mRecycleView;
    @BindView(R.id.fab)
    FloatingActionButton mFloatBtn;
    @BindView(R.id.fl_empty_container)
    FrameLayout flEmptyContainer;

    private List<EmoticonBean> mEmoticonBeans = new ArrayList<>();
    private EmoticonAdapter mEmoticonAdapter;
    private GridLayoutManager mGridLayoutManager;

    private int pageCount = Constant.PAGE_COUNT;// 一次从数据库加载的数据个数
    private int lastVisibleItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        // 申请权限
        requestCameraPermission();
    }

    private void init() {
        initDatas();
        initEvents();
    }

    private void initEvents() {
        mEmoticonAdapter = new EmoticonAdapter(this, mEmoticonBeans, mEmoticonBeans.size() > 0 ? true : false);
        mGridLayoutManager = new GridLayoutManager(this, 2);
        mRecycleView.setLayoutManager(mGridLayoutManager);
        mRecycleView.setAdapter(mEmoticonAdapter);

        mEmoticonAdapter.setOnItemlickListener(new EmoticonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 分享给微信朋友
                String imgPath = mEmoticonBeans.get(position).getEmoticonImgURI();
                WXutil.shareImgToWx(MainActivity.this, imgPath);
            }
        });

        mEmoticonAdapter.setOnItemLongClickListener(new EmoticonAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                popDialog(mEmoticonBeans.get(position), position);
            }
        });

        mFloatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳转到添加表情界面
                jumpToAddActivity();
            }
        });

        checkEmpty();

        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mEmoticonAdapter.isFootHide() == false && lastVisibleItem + 1 == mEmoticonAdapter.getItemCount()) {
                        updateRecyclerView(mEmoticonAdapter.getRealItemCount(), pageCount);
                    }

                    if (mEmoticonAdapter.isFootHide() == true && lastVisibleItem + 2 == mEmoticonAdapter.getItemCount()) {
                        updateRecyclerView(mEmoticonAdapter.getRealItemCount(), pageCount);
                    }
                }


            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mGridLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void popDialog(EmoticonBean bean, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
        EditText editText = view.findViewById(R.id.et_img_content);
        editText.setText(bean.getEmoticonContent());
        editText.setTextColor(Color.BLACK);
        editText.setSelection(bean.getEmoticonContent().length());
        builder.setView(view);
        builder.setTitle("编辑内容");

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {// 添加确定按钮
            @Override
            public void onClick(
                    DialogInterface dialog,
                    int which) {// 确定按钮的响应事件
                LitePal.delete(EmoticonBean.class, bean.getId());
                mEmoticonAdapter.removeItem(position);
                Constant.isDatabaseMotified = true;
                checkEmpty();
            }

        })
                .setNeutralButton("更新内容", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newText = editText.getText().toString().trim();
                        if (!newText.equals(bean.getEmoticonContent())) {
                            EmoticonBean emoticonBean = new EmoticonBean();
                            emoticonBean.setEmoticonContent(newText);
                            emoticonBean.update(bean.getId());
                            mEmoticonAdapter.updateItem(emoticonBean, position);
                            Constant.isDatabaseMotified = true;
                        }
                    }
                })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {// 添加返回按钮

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {// 响应事件
                                dialog.dismiss();

                            }
                        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }


    private void initDatas() {
        // 如果是安装后第一次启动应用，则检查是否有数据库备份文件
        // 取 sharepreference 赋值(Constant.isFirstLanch = true;
        Constant.isFirstLanch = SharedpreferencesUtil.getBoolean(this,SharedpreferencesUtil.KEY_FIRST_LANUCH);
        if (Constant.isFirstLanch) {
            Constant.isFirstLanch = false;
            // 存 sharepreference false
            SharedpreferencesUtil.saveBoolean(this, SharedpreferencesUtil.KEY_FIRST_LANUCH, Constant.isFirstLanch);

            boolean isBackupExist = FileUtil.isFileExist(FileUtil.getSDcardRootPath() +
                    File.separator + Constant.APPLICATION_NAME + File.separator + Constant.BACKUP_DIR_NAME
                    + File.separator + Constant.DATABASE_NAME);
            Log.d(TAG, "---BAckup---exist---" + isBackupExist);
            if (isBackupExist) {
                popRestoreDataDialog();
                return;
            }

        }
        loadDatas();
    }

    private void loadDatas() {
        List<EmoticonBean> pageDataFromDB = getPageDataFromDB(0, pageCount);
        mEmoticonBeans.addAll(pageDataFromDB);
    }

    /**
     * 弹出是否恢复数据库数据对话框
     */
    private void popRestoreDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示!");
        builder.setMessage("检测到上次安装添加的表情，\n是否恢复？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BackupTask backupTask = new BackupTask(MainActivity.this);
                backupTask.setBackupResultListener(new BackupTask.IBackupResultListener() {
                    @Override
                    public void backupSuccess(int type) {
                        if (type == BackupTask.RESTORE_SUCCESS) {
                            loadDatas();
                            mEmoticonAdapter.notifyDataSetChanged();
                            checkEmpty();
                        }
                    }

                    @Override
                    public void backupFailed(int type) {

                    }
                });
                backupTask.execute(BackupTask.COMMAND_RESTORE);
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 删除备份文件夹以及图片文件夹
                FileUtil.deleteDirection(new File(FileUtil.getSDcardRootPath() + File.separator +
                        Constant.APPLICATION_NAME));
                // 取消对话框
                dialogInterface.dismiss();
                // 正常初始化数据库
                loadDatas();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }


    /**
     * 从数据库分页获取数据
     * @param startIndex
     * @param pageCount
     * @return
     */
    private List<EmoticonBean> getPageDataFromDB(int startIndex, int pageCount) {
        List<EmoticonBean> emoticonBeans = LitePal.select(null)
                .limit(pageCount).offset(startIndex).find(EmoticonBean.class);

        return emoticonBeans;
    }

    public void updateRecyclerView(int startIndex, int pageCount) {
        List<EmoticonBean> pageDataFromDB = getPageDataFromDB(startIndex, pageCount);
        if (pageDataFromDB != null && pageDataFromDB.size() > 0) {
            mEmoticonAdapter.updateList(pageDataFromDB, true);
        } else {
            mEmoticonAdapter.updateList(null, false);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryResult(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryResult(newText);
                return false;
            }
        });
        return true;

    }

    private void queryResult(String query) {
        List<EmoticonBean> beans = LitePal.where("emoticonContent like ?", "%" + query + "%").find(EmoticonBean.class);
        mEmoticonBeans.clear();
        mEmoticonBeans.addAll(beans);
        mEmoticonAdapter.notifyDataSetChanged();
        checkEmpty();
    }

    //获取拍照的权限
    private void requestCameraPermission() {
//        判断手机版本,如果低于6.0 则不用申请权限,直接拍照
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0及以上
            Log.i("tag", "手机版本高于6.0，需要申请权限");
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i("tag", "没有权限");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.i("tag", "上次点击了禁止，但没有勾选不再询问");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    Log.i("tag", "第一次启动，或者，上次点击了禁止，并勾选不再询问");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            } else {
                init();
            }
        } else {
            Log.i("tag", "手机是6.0以下的，不需要权限");
            init();
        }

    }

    private void jumpToAddActivity() {
        Intent intent = new Intent(MainActivity.this, AddEmoticonActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "----ACTIVITYRESULT---");
            Constant.isDatabaseMotified = true;
            EmoticonBean bean = LitePal.findLast(EmoticonBean.class);
            mEmoticonAdapter.addItem(bean, mEmoticonAdapter.getRealItemCount());
            checkEmpty();
        }

    }

    //权限申请的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("tag", "" + "权限" + permissions[i] + "申请失败");
                    Toast.makeText(this,
                            "" + "授权失败\n请在设置中打开本应用的读写手机存储权限\n才能用哦~",
                            Toast.LENGTH_LONG).show();
                    this.finish();
                    return;
                }
            }
            init();
        }
    }

    /**
     * 检查当前内容是否为空，设置空界面
     */
    private void checkEmpty() {
        if (mEmoticonAdapter.getRealItemCount() == 0) {
            flEmptyContainer.setVisibility(View.VISIBLE);
        } else {
            flEmptyContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "---db--motified---" + Constant.isDatabaseMotified);
        if (Constant.isDatabaseMotified) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("数据备份中...");
            dialog.setCancelable(false);
            BackupTask backupTask = new BackupTask(this);
            backupTask.setBackupResultListener(new BackupTask.IBackupResultListener() {
                @Override
                public void backupSuccess(int type) {
                    if (type == BackupTask.BACKUP_SUCCESS) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                }

                @Override
                public void backupFailed(int type) {
                    dialog.dismiss();
                    MainActivity.this.finish();
                }
            });
            backupTask.execute(BackupTask.COMMAND_BACKUP);
            dialog.show();

        } else {
            super.onBackPressed();
        }

    }

}
