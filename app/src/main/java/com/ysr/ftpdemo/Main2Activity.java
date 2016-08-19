package com.ysr.ftpdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private Button btnShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initView();
    }

    private void initView() {
        btnShow = (Button) findViewById(R.id.btnShow);
        btnShow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShow:
                Intent intent = new Intent();
                intent.setClass(Main2Activity.this,DialogActivity.class);
                startActivity(intent);
                break;
        }
    }
}
