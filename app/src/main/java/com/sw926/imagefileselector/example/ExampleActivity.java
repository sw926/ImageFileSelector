package com.sw926.imagefileselector.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.sw926.imagefileselector.ImageCropHelper;
import com.sw926.imagefileselector.ImageFileResultListener;
import com.sw926.imagefileselector.ImageFileSelector;
import com.sw926.imagefileselector.ImageUriResultListener;
import java.io.File;

public class ExampleActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImageView;
    private TextView mTvPath;
    private ImageFileSelector mImageFileSelector;

    private EditText mEtWidth;
    private EditText mEtHeight;

    private ImageCropHelper mImageCropHelper;
    private Button mBtnCrop;

    private File mCurrentSelectFile;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.btn_from_sdcard).setOnClickListener(this);
        findViewById(R.id.btn_from_camera).setOnClickListener(this);
        findViewById(R.id.btn_crop).setOnClickListener(this);

        mImageView = findViewById(R.id.iv_image);
        mTvPath = findViewById(R.id.tv_path);
        mEtWidth = findViewById(R.id.et_width);
        mEtHeight = findViewById(R.id.et_height);
        mBtnCrop = findViewById(R.id.btn_crop);

        mImageFileSelector = new ImageFileSelector(this);
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

        mImageCropHelper = new ImageCropHelper(this);

        mImageCropHelper.setListener(new ImageFileResultListener() {
            @Override
            public void onSuccess(@NonNull String filePath) {
                loadImage(filePath);
            }

            @Override
            public void onError() {
                Toast.makeText(ExampleActivity.this, "crop image error", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(ExampleActivity.this, "crop image canceled", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadImage(final String file) {
        new Thread(() -> {
            final Bitmap bitmap = BitmapFactory.decodeFile(file);
            File imageFile = new File(file);
            final StringBuilder builder = new StringBuilder();
            builder.append("path: ");
            builder.append(file);
            builder.append("\n\n");
            builder.append("size: ");
            builder.append((int) (imageFile.length() / 1024d));
            builder.append("KB");
            builder.append("\n\n");
            builder.append("image size: (");
            builder.append(bitmap.getWidth());
            builder.append(", ");
            builder.append(bitmap.getHeight());
            builder.append(")");
            runOnUiThread(() -> {
                mImageView.setImageBitmap(bitmap);
                mTvPath.setText(builder.toString());
            });
        }).start();
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initImageFileSelector() {
        int w = 0;
        if (!TextUtils.isEmpty(mEtWidth.getText().toString())) {
            w = Integer.parseInt(mEtWidth.getText().toString());
        }
        int h = 0;
        if (!TextUtils.isEmpty(mEtHeight.getText().toString())) {
            h = Integer.parseInt(mEtHeight.getText().toString());
        }
        mImageFileSelector.setOutPutImageSize(w, h);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_from_camera: {
                initImageFileSelector();
                mImageFileSelector.takePhoto();
                break;
            }
            case R.id.btn_from_sdcard: {
                initImageFileSelector();
                mImageFileSelector.selectImage();
                break;
            }
            case R.id.btn_crop: {
                if (mCurrentSelectFile != null) {
                    mImageCropHelper.setOutPut(800, 800);
                    mImageCropHelper.setOutPutAspect(1, 1);
                    mImageCropHelper.cropImage(mCurrentSelectFile.getPath());
                }
                break;
            }
        }
    }
}
