[中文说明](README-zh.md)

# ImageFileSelector
##### Use the system software to select, compress, crop images

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=com.sw926.imagefileselector.example)

<a href="http://www.methodscount.com/?lib=com.sw926.imagefileselector%3Alibrary%3A%2B"><img src="https://img.shields.io/badge/Methods and size-core: 273 | deps: 10413 | 33 KB-e91e63.svg"/></a>

#####support Android 2.3+

How to use
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


Select Image
----------
Init
Add to AndroidManifest.xml
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
Add code to you Activity or Fragment
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

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mImageFileSelector.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
}
```
Setting parameters
```java
// Set the output file size
mImageFileSelector.setOutPutImageSize(800, 600);
Set the picture save quality, 0 to 100
mImageFileSelector.setQuality(80)；
```
Start select image
```java
// take picture from camera
mImageFileSelector.takePhoto(this, requestCode);
// select image from sdcard
mImageFileSelector.selectImage(this, requestCode);
// Set the save path of the image，default: /sdcard/Android/data/{packagename}/cache/images/
mImageFileSelector.setOutPutPath();
```


Crop Image
----------
Init
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
Add code to you Activity or Fragment
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
Setting parameters
```java
// Sets the picture aspect ratio
mImageCropper.setOutPutAspect(1, 1);
// Sets the image size
mImageCropper.setOutPut(800, 800);
// Sets whether to scale
mImageCropper.setScale(true);
```
crop image
```java
mImageCropper.cropImage(file);
```
