package com.sw926.imagefileselector.example;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sw926.imagefileselector.ImageCropHelper;
import com.sw926.imagefileselector.ImageFileResultListener;
import com.sw926.imagefileselector.ImageFileSelector;

import com.sw926.imagefileselector.ImageUriResultListener;
import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExampleFragment extends Fragment implements View.OnClickListener {

    private ImageView mImageView;
    private TextView mTvPath;
    private ImageFileSelector mImageFileSelector;

    private EditText mEtWidth;
    private EditText mEtHeight;

    private ImageCropHelper mImageCropHelper;
    private Button mBtnCrop;

    private File mCurrentSelectFile;

    public ExampleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageFileSelector = new ImageFileSelector(this);
        mImageCropHelper = new ImageCropHelper(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_from_sdcard).setOnClickListener(this);
        view.findViewById(R.id.btn_from_camera).setOnClickListener(this);
        view.findViewById(R.id.btn_crop).setOnClickListener(this);


        mImageView = (ImageView) view.findViewById(R.id.iv_image);
        mTvPath = (TextView) view.findViewById(R.id.tv_path);
        mEtWidth = (EditText) view.findViewById(R.id.et_width);
        mEtHeight = (EditText) view.findViewById(R.id.et_height);
        mBtnCrop = (Button) view.findViewById(R.id.btn_crop);

        mImageFileSelector.setListener(new ImageFileResultListener() {
            @Override
            public void onSuccess(@NonNull String filePath) {
                loadImage(filePath);
                mCurrentSelectFile = new File(filePath);
                mBtnCrop.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getContext(), "Canceled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError() {
                Toast.makeText(getContext(), "Unknown Error", Toast.LENGTH_LONG).show();
            }
        });

        mImageCropHelper.setListener(new ImageFileResultListener() {
            @Override
            public void onSuccess(@NonNull String filePath) {
                loadImage(filePath);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getContext(), "crop image canceled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError() {
                Toast.makeText(getContext(), "crop image error", Toast.LENGTH_LONG).show();
            }
        });
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
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mImageView.setImageBitmap(bitmap);
                    mTvPath.setText(builder.toString());
                });
            }
        }).start();
    }
}
