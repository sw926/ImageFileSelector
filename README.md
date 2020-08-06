[中文说明](README-zh.md)

# ImageFileSelector
##### Use the system software to select, compress, crop images

##### support Android Api Level >= 16

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

How to use
----------
Maven

```xml
<dependency>
    <groupId>com.sw926.imagefileselector</groupId>
    <artifactId>library</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>
```
Gradle

```gradle
compile 'com.sw926.imagefileselector:library:1.1.0-SNAPSHOT'
```


Select Image
----------
Init

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
ImageCropper mImageCropper = new ImageCropper();
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
[snap]: https://oss.sonatype.org/content/repositories/snapshots/com/sw926/imagefileselector/library/