package com.sw926.imagefileselector;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.concurrent.Executors;


public class ImageCompressHelper {

    public static class CompressParams implements Parcelable {
        String outputPath;
        int maxWidth = 1000;
        int maxHeight = 1000;
        int saveQuality = 80;
        Bitmap.CompressFormat compressFormat = null;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.outputPath);
            dest.writeInt(this.maxWidth);
            dest.writeInt(this.maxHeight);
            dest.writeInt(this.saveQuality);
            dest.writeInt(this.compressFormat == null ? -1 : this.compressFormat.ordinal());
        }

        public CompressParams() {
        }

        protected CompressParams(Parcel in) {
            this.outputPath = in.readString();
            this.maxWidth = in.readInt();
            this.maxHeight = in.readInt();
            this.saveQuality = in.readInt();
            int tmpCompressFormat = in.readInt();
            this.compressFormat = tmpCompressFormat == -1 ? null : Bitmap.CompressFormat.values()[tmpCompressFormat];
        }

        public static final Parcelable.Creator<CompressParams> CREATOR = new Parcelable.Creator<CompressParams>() {
            @Override
            public CompressParams createFromParcel(Parcel source) {
                return new CompressParams(source);
            }

            @Override
            public CompressParams[] newArray(int size) {
                return new CompressParams[size];
            }
        };
    }


    public static class CompressJop implements Parcelable {
        String inputFile;
        CompressParams params;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.inputFile);
            dest.writeParcelable(this.params, flags);
        }

        public CompressJop() {
        }

        protected CompressJop(Parcel in) {
            this.inputFile = in.readString();
            this.params = in.readParcelable(CompressParams.class.getClassLoader());
        }

        public static final Parcelable.Creator<CompressJop> CREATOR = new Parcelable.Creator<CompressJop>() {
            @Override
            public CompressJop createFromParcel(Parcel source) {
                return new CompressJop(source);
            }

            @Override
            public CompressJop[] newArray(int size) {
                return new CompressJop[size];
            }
        };
    }

    static String TAG = ImageCompressHelper.class.getSimpleName();

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void compress(CompressJop jop) {
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
