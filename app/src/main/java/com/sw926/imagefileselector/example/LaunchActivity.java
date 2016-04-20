package com.sw926.imagefileselector.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_activity: {
                startActivity(new Intent(this, ExampleActivity.class));
                break;
            }
            case R.id.btn_fragment: {
                startActivity(new Intent(this, ExampleFragmentActivity.class));
                break;
            }
            default:
                break;
        }
    }
}
