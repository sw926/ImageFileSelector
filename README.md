# ImageFileSelector
##### 轻量级的选取图片和裁切图片的库，使用系统自带的软件实现。

[![Build Status](https://travis-ci.org/sw926/ImageFileSelector.svg?branch=master)](https://travis-ci.org/sw926/ImageFileSelector)

#####支持Android版本：Android 2.2+

选取图片
----------
初始化
``` java
ImageFileSelector mImageFileSelector = new ImageFileSelector(this);
mImageFileSelector.setCallback(new ImageFileSelector.Callback() {
    @Override
    public void onSuccess(final String file) {
        // 选取图片成功
    }

    @Override
    public void onError() {
        // 选取图片失败
    }
});
```
在Activity中，加入以下代码
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mImageFileSelector.onActivityResult(requestCode, resultCode, data);
}

@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mImageFileSelector.onSaveInstanceState(outState);
}

@Override
protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mImageFileSelector.onRestoreInstanceState(savedInstanceState);
}

// Android 6.0的动态权限
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mImageFileSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
}
```
设置参数
```java
// 设置输出文件的尺寸
mImageFileSelector.setOutPutImageSize(800, 600);
// 设置保存图片的质量 0到100
mImageFileSelector.setQuality(80)；
```
现在开始选取图片
```java
// 拍照选取
mImageFileSelector.takePhoto(this);
// 从文件选取
mImageFileSelector.selectImage(this);
```
所有选取的图片都在
```
/sdcard/Android/data/{packagename}/cache/image/image_selector
```
使用完记得删除

裁切图片
----------
初始化
```java
ImageCropper mImageCropper = new ImageCropper(this);
mImageCropper.setCallback(new ImageCropper.ImageCropperCallback() {
    @Override
    public void onCropperCallback(ImageCropper.CropperResult result, File srcFile, File outFile) {
        if (result == ImageCropper.CropperResult.success) {
            // 成功
        } else if (result == ImageCropper.CropperResult.error_illegal_input_file) {
            // 输入的文件失败
        } else if (result == ImageCropper.CropperResult.error_illegal_out_file) {
            // 输出文件失败
        }
    }
});
```
在Activit中加入以下代码
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mImageCropper.onActivityResult(requestCode, resultCode, data);
}

@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mImageCropper.onSaveInstanceState(outState);
}

@Override
protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mImageCropper.onRestoreInstanceState(savedInstanceState);
}
```
设置参数
```java
// 设置输出文件的宽高比
mImageCropper.setOutPutAspect(1, 1);
// 设置输出文件的尺寸
mImageCropper.setOutPut(800, 800);
// 设置是否缩放到支持的尺寸
mImageCropper.setScale(true);
```
裁切图片
```java
mImageCropper.cropImage(file);
```
