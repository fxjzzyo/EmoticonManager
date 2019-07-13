package com.fxjzzyo.emoticonmanager.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.fxjzzyo.emoticonmanager.MyApplication;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXEmojiObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;

import java.io.ByteArrayOutputStream;

/**
 * Created by fxjzzyo
 * on date 2019/7/13 0013
 */
public class WXutil {
    private static int mTargetScene = SendMessageToWX.Req.WXSceneSession;

    public static boolean shareImgToWx(Context context, String imgUri) {

        if (!MyApplication.api.isWXAppInstalled()) {
            Toast.makeText(context, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Constant.isNetAvaiable) {
            Toast.makeText(context, "请检查您的网络连接", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(imgUri.endsWith(".gif")){
            return shareGifImg(context,imgUri);
        }else{
            return shareNormalImg(context,imgUri);
        }

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


    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 以最省内存的方式读取本地资源的图片 或者SDCard中的图片
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
//        return compressBitmap(bitmap);
        return bitmap;
    }

    /**
     * 质量压缩
     * @param bitmap
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 10;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        bitmap.recycle();
        byte[] bytes = baos.toByteArray();
        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        Log.d("wechat", "压缩后图片的大小" + (bm.getByteCount() / 1024 / 1024) + "M宽度为" + bm.getWidth() + "高度为" + bm.getHeight()+"bytes.length= " +
//                (bytes.length / 1024) + "KB"+"quality=" + quality);
        return bm;

    }
}