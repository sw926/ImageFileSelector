package com.sw926.imagefileselector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;

class ImageCompressHelper {

    private static final String TAG = ImageCompressHelper.class.getSimpleName();

    private int mMaxWidth = 1000;
    private int mMaxHeight = 1000;
    private int mMinFileSize = 150 * 1024;
    private int mQuality = 80;

    private Context mContext;
    private CompressCallback mCallback;

    public ImageCompressHelper(Context context) {
        mContext = context;
    }

    public void setCallback(CompressCallback callback) {
        mCallback = callback;
    }

    /**
     * 设置一个最小的文件大小，小于该大小的文件不进行压缩
     *
     * @param minFileSize 最小文件字节数
     */
    @SuppressWarnings("unused")
    public void setMinFileSize(int minFileSize) {
        mMinFileSize = minFileSize;
    }

    /**
     * 设置压缩后的文件大小
     *
     * @param maxWidth  压缩后文件宽度
     * @param maxHeight 压缩后文件高度
     */
    @SuppressWarnings("unused")
    public void setOutPutImageSize(int maxWidth, int maxHeight) {
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
    }

    /**
     * 设置压缩后保存图片的质量
     *
     * @param quality 图片质量 0 - 100
     */
    @SuppressWarnings("unused")
    public void setQuality(int quality) {
        mQuality = quality;
    }

    public void compress(String fileName, boolean deleteSrc) {
        if (mMaxHeight <= 0 || mMaxWidth <= 0) {
            if (mCallback != null) {
                File outputFile = CommonUtils.generateExternalImageCacheFile(mContext, ".jpg");
                CommonUtils.copy(new File(fileName), outputFile);
                mCallback.onCallBack(outputFile.getAbsolutePath());
            }
        } else {
            ImageFile srcImageFile = new ImageFile(fileName, deleteSrc);
            new CompressTask().execute(srcImageFile);
        }
    }

    private class CompressTask extends AsyncTask<ImageFile, Integer, String> {

        @Override
        protected String doInBackground(ImageFile... params) {
            ImageFile srcFileInfo = params[0];
            File outputFile = CommonUtils.generateExternalImageCacheFile(mContext, ".jpg");
            File srcFile = new File(srcFileInfo.mSrcFilePath);
            if (srcFile.length() < mMinFileSize) {
                // 小于指定尺寸，直接copy
                CommonUtils.copy(srcFile, outputFile);
            } else {
                boolean isCompress = compressImageFile(srcFileInfo.mSrcFilePath, outputFile.getPath(), mMaxWidth, mMaxHeight, mQuality);
                if (!isCompress) {
                    // 没有压缩，直接copy
                    CommonUtils.copy(srcFile, outputFile);
                }
            }
            if (srcFileInfo.mDeleteSrc) {
                //noinspection ResultOfMethodCallIgnored
                srcFile.delete();
            }
            return outputFile.getPath();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mCallback != null) {
                mCallback.onCallBack(result);
            }
        }

    }

    /**
     * 压缩图片文件
     *
     * @param srcFile   源文件
     * @param dstFile   目标文件
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return true进行了压缩，false无需压缩
     */
    public static boolean compressImageFile(String srcFile, String dstFile, int maxWidth, int maxHeight, int quality) {

        AppLogger.i(TAG, "------------------ start compress file ------------------");
        AppLogger.i(TAG, "compress file:" + srcFile);
        AppLogger.i(TAG, "file length:" + (int) (new File(srcFile).length() / 1024d) + "kb");
        AppLogger.i(TAG, "output size:(" + maxWidth + ", " + maxHeight + ")");

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(srcFile, decodeOptions);
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;

        AppLogger.i(TAG, "input size:(" + actualWidth + ", " + actualHeight + ")");

        if (actualWidth < maxWidth && actualHeight < maxHeight) {
            AppLogger.w(TAG, "stop compress: input size < output size");
            return false;
        }

        int sampleSize;
        int w;
        int h;
        if (actualWidth * maxHeight > maxWidth * actualHeight) {
            w = maxWidth;
            h = (int) (w * actualHeight / (double) actualWidth);
            sampleSize = (int) (actualWidth / (double) maxWidth);
        } else {
            h = maxHeight;
            w = (int) (h * actualWidth / (double) actualHeight);
            sampleSize = (int) (actualHeight / (double) maxHeight);
        }

        AppLogger.i(TAG, "in simple size:" + sampleSize);

        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = sampleSize;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(srcFile, decodeOptions);
        } catch (OutOfMemoryError error) {
            AppLogger.e(error.getMessage());
        }

        if (bitmap == null) {
            AppLogger.e(TAG, "stop compress:decode file error");
            return false;
        }

        AppLogger.i(TAG, "origin bitmap size:(" + bitmap.getWidth() + ", " + bitmap.getHeight() + ")");

        if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
            Bitmap tempBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            bitmap.recycle();
            bitmap = tempBitmap;
            AppLogger.i(TAG, "scale down:(" + bitmap.getWidth() + ", " + bitmap.getHeight() + ")");
        }


        int degree = ImageUtils.getExifOrientation(srcFile);
        if (degree != 0) {
            AppLogger.i(TAG, "rotate image from:" + degree);
            Bitmap rotate = ImageUtils.rotateImage(degree, bitmap);
            bitmap.recycle();
            bitmap = rotate;
        }

        ImageUtils.saveBitmap(bitmap, dstFile, Bitmap.CompressFormat.JPEG, quality);

        AppLogger.i(TAG, "output file length:" + (int) (new File(dstFile).length() / 1024d) + "kb");
        AppLogger.i(TAG, "------------------ compress file complete ---------------");
        return true;
    }

    public interface CompressCallback {
        void onCallBack(String outFile);
    }

    private class ImageFile {
        public final String mSrcFilePath;
        public final boolean mDeleteSrc;

        public ImageFile(String srcFilePath, boolean deleteSrc) {
            mSrcFilePath = srcFilePath;
            mDeleteSrc = deleteSrc;
        }
    }
}
