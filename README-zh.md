# ImageFileSelector
##### 轻量级的选取图片和裁切图片的库，使用系统自带的软件实现。

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=com.sw926.imagefileselector.example)

<a href="http://www.methodscount.com/?lib=com.sw926.imagefileselector%3Alibrary%3A%2B"><img src="https://img.shields.io/badge/Methods and size-core: 363 | deps: 16079 | 53 KB-e91e63.svg"/></a>

#####支持Android版本：Android 2.3+

使用方法
----------
Maven

```xml
<dependency>
    <groupId>com.sw926.imagefileselector</groupId>
    <artifactId>library</artifactId>
    <version>1.0.6</version>
</dependency>
```
Gradle

```gradle
compile 'com.sw926.imagefileselector:library:1.0.6'
```


选取图片
----------
初始化

添加到AndroidManifest.xml
```
<application>
    ...
    <provider
        android:name="android.support.v4.content.FileProvider"
        android:authorities="{your pacekage name}.com.sw926.imagefileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/files"/>
    </provider>
    ...
</application>
```

``` java
ImageFileSelector mImageFileSelector = new ImageFileSelector(this);
mImageFileSelector.setCallback(new ImageFileSelector.Callback() {
    @Override
    public void onError(@NotNull ErrorResult errorResult) {
        switch (errorResult) {
            case permissionDenied:
                break;
            case canceled:
                break;
            case error:
                break;
        }
    }

    @Override
    public void onSuccess(@NotNull String file) {
    }
});
```
在Activity中，加入以下代码
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mImageFileSelector.onActivityResult(this, requestCode, resultCode, data);
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
    mImageFileSelector.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
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
mImageFileSelector.takePhoto(this, requestCode);
// 从文件选取
mImageFileSelector.selectImage(this, requestCode);
// 设置图片输出路径，默认/sdcard/Android/data/{packagename}/cache/images/
mImageFileSelector.setOutPutPath();
```


裁切图片
----------
初始化
```java
ImageCropper mImageCropper = new ImageCropper(this);
mImageCropper.setCallback(new ImageCropper.ImageCropperCallback() {
        @Override
        public void onError(@NotNull ImageCropper.CropperErrorResult result) {
            switch (result) {
                case error:
                    break;
                case canceled:
                    break;
                case notSupport:
                    break;
            }
        }

        @Override
        public void onSuccess(@NotNull String outputFile) {
        }
    });
```
在Activity或Fragment中加入以下代码
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
// 设置是否缩放到指定的尺寸
mImageCropper.setScale(true);
```
裁切图片
```java
mImageCropper.cropImage(file);
```
