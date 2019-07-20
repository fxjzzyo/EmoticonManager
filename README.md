# EmoticonManager

## 1. 简介
这是一个表情管理工具 app。

可以从本地选取图片，并添加描述。通过搜索关键字迅速查询图片，
从而解决微信聊天表情查找困难，表情收藏上限的问题。

## 2. 功能

- 添加图片
- 删除图片
- 编辑图片内容描述
- 分享图片到微信朋友，包括gif图片

## 3. 待实现

- 暂无

## 技术点
- 主体采用material design 设计风格
- 数据存储使用litepal数据库框架
- 图片加载使用glide框架
- 自定义behavior实现floatButton随appBarLayout上滑下滑

## 4. 更新日志

- 2019.7.10 实现主体功能

- 2019.7.13 
  - 实现分享到微信朋友功能，至此完成app所有主要的功能点
  - 自定义behavior实现floatButton随appBarLayout上滑下滑
  
- 2019.7.19
  - 新增数据库备份恢复机制
  - 让分享静态图片也走分享gif表情路线，使得分享后表情下面没有应用logo标记
  - 实现数据库分页加载
  - 至此完成v1.0版本
  
- 2019.7.20
  - 修复了一些bug
 
## 5. 难点记录
分享gif图片到微信的功能折腾了不少，按正常图片分享（使用WXImageObject）的话，只能显示静态图。

后来发现有个WXEmojiObject，这不就正是分享表情的类嘛，连忙稍加替换，验证...发现连静态的也出不来了。

本来都要放弃分享gif图了，可苍天不负有心人。无意中加了一段看似没用的代码：
```
//设置缩略图
Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_input_add);
msg.thumbData = bmpToByteArray(bitmap, true);
```
没错，就是设置了个缩略图，分享的时候都会弹出个对话框让你输入“说点什么吧”，上面有个图片的预览。这个预览图片就是缩略图。

可是这段代码的缩略图就是随便用系统的一张图片弄的啊，跟要分享的图片木有半点关系么。

是的，就是没关系。可加了这段代码，就能分享了，缩略图还显示对了，就是你要分享的gif图片，你说奇怪不奇怪。

