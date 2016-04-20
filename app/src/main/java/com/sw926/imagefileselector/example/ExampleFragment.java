package com.sw926.imagefileselector.example;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sw926.imagefileselector.ImageCropper;
import com.sw926.imagefileselector.ImageFileSelector;

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

    private ImageCropper mImageCropper;
    private Button mBtnCrop;

    private File mCurrentSelectFile;

    public ExampleFragment() {
        // Required empty public constructor
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

        mImageFileSelector = new ImageFileSelector(getContext());
        mImageFileSelector.setCallback(new ImageFileSelector.Callback() {
            @Override
            public void onSuccess(final String file) {
                if (!TextUtils.isEmpty(file)) {
                    loadImage(file);
                    mCurrentSelectFile = new File(file);
                    mBtnCrop.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "select image file error", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError() {
                Toast.makeText(getContext(), "select image file error", Toast.LENGTH_LONG).show();
            }
        });

        mImageCropper = new ImageCropper(this);
        mImageCropper.setCallback(new ImageCropper.ImageCropperCallback() {
            @Override
            public void onCropperCallback(ImageCropper.CropperResult result, File srcFile, File outFile) {
                mCurrentSelectFile = null;
                mBtnCrop.setVisibility(View.GONE);
                if (result == ImageCropper.CropperResult.success) {
                    loadImage(outFile.getPath());
                } else if (result == ImageCropper.CropperResult.error_illegal_input_file) {
                    Toast.makeText(getContext(), "input file error", Toast.LENGTH_LONG).show();
                } else if (result == ImageCropper.CropperResult.error_illegal_out_file) {
                    Toast.makeText(getContext(), "output file error", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_from_camera: {
                initImageFileSelector();
                mImageFileSelector.takePhoto(this);
                break;
            }
            case R.id.btn_from_sdcard: {
                initImageFileSelector();
                mImageFileSelector.selectImage(this);
                break;
            }
            case R.id.btn_crop: {
                if (mCurrentSelectFile != null) {
                    mImageCropper.setOutPut(800, 800);
                    mImageCropper.setOutPutAspect(1, 1);
                    mImageCropper.cropImage(mCurrentSelectFile);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = BitmapFactory.decodeFile(file);
                File imageFile = new File(file);
                final StringBuilder builder = new StringBuilder();
                builder.append("path: ");
                builder.append(file);
                builder.append("\n\n");
                builder.append("length: ");
                builder.append((int) (imageFile.length() / 1024d));
                builder.append("KB");
                builder.append("\n\n");
                builder.append("image size: (");
                builder.append(bitmap.getWidth());
                builder.append(", ");
                builder.append(bitmap.getHeight());
                builder.append(")");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                        mTvPath.setText(builder.toString());
                    }
                });
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageFileSelector.onActivityResult(requestCode, resultCode, data);
        mImageCropper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mImageFileSelector.onSaveInstanceState(outState);
        mImageCropper.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mImageFileSelector.onRestoreInstanceState(savedInstanceState);
        mImageCropper.onRestoreInstanceState(savedInstanceState);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mImageFileSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
