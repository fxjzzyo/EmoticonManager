package com.fxjzzyo.emoticonmanager.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fxjzzyo.emoticonmanager.R;
import com.fxjzzyo.emoticonmanager.bean.EmoticonBean;
import com.fxjzzyo.emoticonmanager.util.Constant;
import com.fxjzzyo.emoticonmanager.util.FileUtil;

import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddEmoticonActivity extends AppCompatActivity {

    public static final String TAG = "tag";
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    @BindView(R.id.iv_add_img)
    ImageView ivImg;
    @BindView(R.id.et_img_content)
    EditText etImgContent;
    @BindView(R.id.tl)
    Toolbar toolbar;

    private Uri imageUri;// 拍照获得的临时图片uri
    private Bitmap mBitmap;// 拍照获得的临时图片
    private String mImgContent;// 图片内容
    private String mImagePath;// 从相册选取到的图片路径
    private boolean isTakePhoto;// 标识拍照还是选择相册

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emoticon);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }


    @OnClick(R.id.btn_chose_from)
    public void choosePicFrom() {
        Log.d(TAG, "choose pic from local");
        openAlbum();
    }

    @OnClick(R.id.btn_take_photo)
    public void takePhoto() {
        Log.d(TAG, "take pic");
        startTake();
    }

    @OnClick(R.id.btn_confirm)
    public void confirm() {
        Log.d(TAG, "btn confirm");
        // 检查内容描述
        if (checkContentEmpty()) {
            return;
        }
        String imgPath = null;
        if (isTakePhoto) {
            // 保存图片到本应用的图片目录下
            imgPath = saveImgToLocal(mBitmap);
        } else {
            // 拷贝图片到本应用的图片目录下
            imgPath = copyImgTolocal();
        }
        this.mImagePath = imgPath;
        // 检查图片
        if (checkImgEmpty()) {
            return;
        }
        // 将图片路径写入数据库
        writeImgPathToDB(imgPath);
        setResult(RESULT_OK);
        // 返回 main activity
        this.finish();
    }

    private boolean checkContentEmpty() {
        mImgContent = etImgContent.getText().toString().trim();
        if (TextUtils.isEmpty(mImgContent)) {
            Toast.makeText(this, "请输入图片描述~", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean checkImgEmpty() {
        if (TextUtils.isEmpty(mImagePath)) {
            Log.d(TAG, "--imgpath---" + mImagePath);
            Toast.makeText(this, "请添加图片~", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    /**
     * 保存到数据库
     *
     * @param imgPath
     */
    private void writeImgPathToDB(String imgPath) {
        Log.d(TAG, "--todb--img--path---" + imgPath);
        EmoticonBean emoticonBean = new EmoticonBean();
        emoticonBean.setEmoticonContent(mImgContent);
        emoticonBean.setEmoticonImgURI(imgPath);
        emoticonBean.save();
    }

    private String copyImgTolocal() {
        if (mImagePath != null) {
            if (FileUtil.copyFile(mImagePath, getImgFoldPath())) {
                String imgName = mImagePath.substring(mImagePath.lastIndexOf("/"));
                return getImgFoldPath() + imgName;
            }
        }
        return null;
    }

    private String getImgFoldPath() {
        return FileUtil.getSDcardRootPath() + File.separator + Constant.IMAGE_DIR_NAME;
    }

    private String saveImgToLocal(Bitmap bitmap) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String t = format.format(new Date());
        String imgName = t + ".jpg";
        Log.d(TAG, "---imgName----" + imgName);
        Log.d(TAG, "---imgfoldPath----" + getImgFoldPath());
        FileUtil.saveBitmapToFile(bitmap, getImgFoldPath(), imgName);
        // 记录图片在本地的路径
        String path = getImgFoldPath() + File.separator + imgName;
        return path;
    }

    @OnClick(R.id.btn_cancel)
    public void cancel() {
        Log.d(TAG, "btn cancel");
        setResult(RESULT_CANCELED);
        this.finish();
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);

    }

    private void startTake() {
        File outputImage = new File(getExternalCacheDir(), "output_img.jpg");

        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(this, "com.fxjzzyo.emoticonmanager.fileprovider", outputImage);

        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 将拍摄的照片显示出来
                    try {
                        Log.d(TAG, "----imageuri-----" + imageUri);
                        Bitmap bm0 = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        // 旋转90度
                        Matrix m = new Matrix();
                        m.setRotate(90, (float) bm0.getWidth() / 2, (float) bm0.getHeight() / 2);
                        mBitmap = Bitmap.createBitmap(bm0, 0, 0, bm0.getWidth(), bm0.getHeight(), m, true);
                        ivImg.setImageBitmap(mBitmap);
                        isTakePhoto = true;

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用下面方法处理图片
                        Log.d(TAG, "大于19的 api");
                        handleImageOnKitKat(data);
                    } else {
                        Log.d(TAG, "小于19的 api");
                        // 4.4及以下系统使用下面方法处理图片
                        handleImageBeforeKitKat(data);
                        Log.d(TAG, "小于19的 api");
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是 document 类型的 uri，则通过 document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            Log.d(TAG, "Document type uri, doc id " + docId);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];// 解析出数字格式的 id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                Log.d(TAG, "selection--->" + selection);

                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                Log.d(TAG, "content uri--->" + contentUri);

                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.d(TAG, "content");

            // 如果是 content 类型的 uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Log.d(TAG, "file");

            // 如果是 file 类型的 uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath);// 根据路径显示图片
    }

    private void displayImage(String imagePath) {
        Log.d(TAG, "imagePath--->" + imagePath);

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            ivImg.setImageBitmap(bitmap);
            this.mImagePath = imagePath;
            isTakePhoto = false;
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getImagePath(Uri externalContentUri, String selection) {
        String path = null;
        // 通过 uri 和 selection 来获取真是的图片路径
        Cursor cursor = getContentResolver().query(externalContentUri, null, selection, null, null);
        if (cursor != null) {
            Log.d(TAG, "cursor not null");

            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Log.d(TAG, "path-->" + path);

            }
            cursor.close();
        }
        return path;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
