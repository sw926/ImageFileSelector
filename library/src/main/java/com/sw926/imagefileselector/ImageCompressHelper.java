package com.sw926.imagefileselector;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;

import java.io.File;
import java.util.concurrent.Executors;


class ImageCompressHelper {

    public static class CompressParams {
        String outputPath;
        int maxWidth = 1000;
        int maxHeight = 1000;
        int saveQuality = 80;
        Bitmap.CompressFormat compressFormat = null;
    }


    public static class CompressJop {
        String inputFile;
        CompressParams params;
    }

    static String TAG = ImageCompressHelper.class.getSimpleName();

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    void compress(CompressJop jop) {
        if (Build.VERSION.SDK_INT >= 11) {
            new CompressTask().executeOnExecutor(Executors.newCachedThreadPool(), jop);
        } else {
            new CompressTask().execute(jop);
        }
    }

    private class CompressTask extends AsyncTask<CompressJop, Integer, String> {

        @Override
        protected String doInBackground(CompressJop... jops) {
            AppLogger.i(TAG, "------------------ start compress file ------------------");
            CompressJop jop = jops[0];
            CompressParams param = jop.params;

            Bitmap.CompressFormat format = param.compressFormat == null ? CompressFormatUtils.parseFormat(jop.inputFile) : param.compressFormat;
            AppLogger.i(TAG, "use compress format:" + format.name());

            File parentDir = new File(param.outputPath);
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            File outputFile = new File(parentDir, "img_" + System.currentTimeMillis() + CompressFormatUtils.getExt(format));
            Bitmap bitmap = ImageUtils.compressImageFile(jop.inputFile, param.maxWidth, param.maxHeight);
            if (bitmap != null) {
                ImageUtils.saveBitmap(bitmap, outputFile.getPath(), format, param.saveQuality);
                if (outputFile.exists()) {
                    AppLogger.i(TAG, "compress success, output file: " + outputFile.getPath());
                    return outputFile.getPath();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                if (mCallback != null) {
                    mCallback.onSuccess(result);
                }
            } else {
                if (mCallback != null) {
                    mCallback.onError();
                }
            }
        }

    }

    interface Callback {
        void onError();

        void onSuccess(String file);
    }

}
