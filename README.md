# ImageFileSelector

轻量级的选取图片和裁切图片的库，使用系统自带的软件实现。

支持Android版本 Api Level >= 16

## 使用方法

```gradle
compile 'com.sw926.imagefileselector:library:2.0.0'
```

在 Activity 或者 Fragment Start 之前进行初始化：

``` java
mImageFileSelector = new ImageFileSelector(this);
mImageFileSelector.setOutPutImageSize(w, h);
mImageFileSelector.setQuality(80);
mImageFileSelector.setListener(new ImageFileResultListener() {
    @Override
    public void onSuccess(@NonNull String filePath) {
        loadImage(filePath);
        mCurrentSelectFile = new File(filePath);
        mBtnCrop.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancel() {
        Toast.makeText(ExampleActivity.this, "Canceled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError() {
        Toast.makeText(ExampleActivity.this, "Unknown Error", Toast.LENGTH_LONG).show();
    }
});
```

在选择图片的地方调用：

``` java
mImageFileSelector.takePhoto();
// 或者
mImageFileSelector.selectImage();
```

## 说明

在 appcompat 支持 ActivityResultLauncher 后，选择图片已经非常简单，这个项目只是做了简单的封装，添加了拍照和图片压缩。选择图片不需要任何权限，如果 App 的 `AndroidManifest.xml` 没有添加 `<uses-permission android:name="android.permission.CAMERA" />`
，拍照也不需要申请权限。