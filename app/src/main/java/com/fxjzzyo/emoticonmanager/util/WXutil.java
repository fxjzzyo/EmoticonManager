package com.fxjzzyo.emoticonmanager.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.fxjzzyo.emoticonmanager.MyApplication;
import com.fxjzzyo.emoticonmanager.R;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXEmojiObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by fxjzzyo
 * on date 2019/7/13 0013
 */
public class WXutil {
    private static int mTargetScene = SendMessageToWX.Req.WXSceneSession;

    public static boolean shareImgToWx(Context context, String imgUri) {

        if (!MyApplication.api.isWXAppInstalled()) {
            Toast.makeText(context, R.string.weichat_not_exist, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Constant.isNetAvaiable) {
            Toast.makeText(context, R.string.check_network, Toast.LENGTH_SHORT).show();
            return false;
        }

//        if(imgUri.endsWith(".gif")){
//            return shareGifImg(context,imgUri);
//        }else{
//            return shareNormalImg(context,imgUri);
//        }
        return shareEmoji(context, imgUri);

    }

    private static boolean shareNormalImg(Context context, String imgUri) {
        // 初始化 WXImageObject对象
        WXImageObject imgObj = new WXImageObject();
        imgObj.imagePath = imgUri;
        // 初始化 WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = mTargetScene;

        //调用api接口，发送数据到微信
        return MyApplication.api.sendReq(req);
    }

    private static boolean shareGifImg(Context context, String imgUri) {
        // 初始化 WXEmojiObject
        WXEmojiObject emojiObject = new WXEmojiObject();
        emojiObject.emojiPath = imgUri;
        // 初始化 WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = emojiObject;
        //设置缩略图
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_input_add);
        msg.thumbData = bmpToByteArray(bitmap, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("emoji");
        req.message = msg;
        req.scene = mTargetScene;

        //调用api接口，发送数据到微信
        return MyApplication.api.sendReq(req);

    }


    private static boolean shareEmoji(Context context, String imgUri) {

        // 初始化 WXEmojiObject
        WXEmojiObject emojiObject = new WXEmojiObject();
        /*if (imgUri.endsWith(".gif")) {
            // gif 动图就直接从本地路径读取
            emojiObject.emojiPath = imgUri;
        } else {
            // 一般图片从本地读取后，压缩成 PNG格式 再发送
            Bitmap localImg = getLocalImg(imgUri);
            emojiObject.emojiData = bmpToByteArray(localImg, true);
        }*/
        // 图片直接从本地路径读取
        emojiObject.emojiPath = imgUri;

        // 初始化 WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = emojiObject;
        //设置缩略图
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_input_add);
        msg.thumbData = bmpToByteArray(bitmap, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("emoji");
        req.message = msg;
        req.scene = mTargetScene;

        //调用api接口，发送数据到微信
        return MyApplication.api.sendReq(req);

    }

    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 将 bitmap 转换成 byteArray,同时压缩
     *
     * @param bmp
     * @param needRecycle
     * @return
     */
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int quality = 100;
        bmp.compress(Bitmap.CompressFormat.PNG, quality, output);
        // 质量压缩，保证质量不小于10的前提下，尽量压缩到 byte 长度不大于最大长度
        while (quality > 10 && output.toByteArray().length > Constant.CONTENT_LENGTH_LIMIT) {
            output.reset();
            quality -= 10;
            bmp.compress(Bitmap.CompressFormat.PNG, quality, output);
        }
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        Log.d("tag", "---压缩后 byte 长度----" + result.length);
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 以最省内存的方式读取本地资源的图片 或者SDCard中的图片
     *
     * @param imagePath 图片在SDCard中的路径
     * @return
     */
    public static Bitmap getLocalImg(String imagePath) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opt);
        return bitmap;
    }

    /**
     * 质量压缩
     * 压缩成 png 格式 且不大于100KB
     *
     * @param bitmap
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;// 100表示不压缩 值越小，压缩后图片越小
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);
            quality -= 10;
        }
        bitmap.recycle();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Bitmap bm = BitmapFactory.decodeStream(bis);
//        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.d("tag", "压缩后图片的大小" + (bm.getByteCount() / 1024 / 1024) + "M宽度为" + bm.getWidth() + "高度为" + bm.getHeight() + "bytes.length= " +
                bytes.length + "B" + "quality=" + quality);
        return bm;

    }

    /**
     * 获取 bitmap 的 byteArray 长度
     *
     * @param bitmap
     * @return
     */
    public static int getBitymapBytesLength(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buf);

        byte[] byteArray = buf.array();
        buf.reset();
        return byteArray.length;
    }

    /**
     * 得到bitmap所占内存大小
     */
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }
        // 在低版本中用一行的字节x高度
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }
}