package com.ysr.ftpdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ysr.ftpdemo.ftp.FtpUtils;
import com.ysr.ftpdemo.utils.ConfigEntity;
import com.ysr.ftpdemo.utils.ConfigUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
    public static final String FTP_CONNECT_FAIL = "ftp连接失败";
    public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
    public static final String FTP_FILE_NOTEXISTS = "ftp上文件不存在";
    public static final String FTP_UPLOAD_SUCCESS = "ftp文件上传成功";
    public static final String FTP_UPLOAD_FAIL = "ftp文件上传失败";
    public static final String FTP_UPLOAD_LOADING = "ftp文件正在上传";
    public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
    public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
    public static final String FTP_DOWN_FAIL = "ftp文件下载失败";
    public static final String FTP_DELETEFILE_SUCCESS = "ftp文件删除成功";
    public static final String FTP_DELETEFILE_FAIL = "ftp文件删除失败";
    public static final int FILE_SELECT_CODDE = 0x1004;


    private EditText etUrl;
    private EditText etPort;
    private EditText etUserName;
    private EditText etPassWord;
    private Button btnSure;
    private Button btnLine;
    private Button btnNext;
    private ConfigEntity configEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();

        //初始化数据
        configEntity = ConfigUtils.loadftpConfig(context);
        etUrl.setText(configEntity.ftpIp);
        etPort.setText(configEntity.ftpPort + "");
        etUserName.setText(configEntity.ftpUser);
        etPassWord.setText(configEntity.ftpPassWord);
    }

    private void initView() {
        etUrl = (EditText) findViewById(R.id.etUrl);
        etPort = (EditText) findViewById(R.id.etPort);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassWord = (EditText) findViewById(R.id.etPassWord);
        btnSure = (Button) findViewById(R.id.btnSure);
        btnLine = (Button) findViewById(R.id.btnLine);

        btnNext = (Button) findViewById(R.id.btnNext);
        btnSure.setOnClickListener(this);
        btnLine.setOnClickListener(this);

        btnNext.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSure:
                submit();
                break;
            case R.id.btnNext:
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(i);
                break;
            case R.id.btnLine:
                final String etUrlString;
                final String etPortString;
                final String etUserNameString;
                final String etPassWordString;
                etUrlString = etUrl.getText().toString().trim();
                etPortString = etPort.getText().toString().trim();
                etUserNameString = etUserName.getText().toString().trim();
                etPassWordString = etPassWord.getText().toString().trim();
                //主线程不允许请求网络
                new Thread() {
                    @Override
                    public void run() {
                        boolean flag = FtpUtils.TestFtp(etUrlString, Integer.parseInt(etPortString), etUserNameString, etPassWordString, "");

                        if (flag) {
                            Log.i("flag", "连接成功!");
                            //子线程使用toast
                            Message msg = msgHandler.obtainMessage();
                            msg.arg1 = R.string.network_success;
                            msgHandler.sendMessage(msg);
                        } else {
                            Message msg = msgHandler.obtainMessage();
                            Log.i("flag", "连接失败!");
                            msg.arg1 = R.string.network_fail;
                            msgHandler.sendMessage(msg);
                        }

                    }
                }.start();


                break;


        }
    }

    private final Handler msgHandler = new Handler() {
        public void handleMessage(Message msg) {
         //   Toast.makeText(getApplicationContext(), msg.arg1, Toast.LENGTH_SHORT).show();

            switch (msg.arg1) {
                case R.string.network_success:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_success), Toast.LENGTH_SHORT).show();
                    break;
                case R.string.network_fail:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_fail), Toast.LENGTH_SHORT).show();
                    break;
                case R.string.post_success:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.post_success), Toast.LENGTH_SHORT).show();
                    break;
                case R.string.post_fail:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.post_fail), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    //修改数据
    private void submit() {
        // validate
        String etUserNameString;
        String etPassWordString;
        String etUrlString;
        String etPortString;
        etUrlString = etUrl.getText().toString().trim();
        etPortString = etPort.getText().toString().trim();
        etUserNameString = etUserName.getText().toString().trim();
        etPassWordString = etPassWord.getText().toString().trim();
        if (TextUtils.isEmpty(etUrlString) || TextUtils.isEmpty(etPortString) || TextUtils.isEmpty(etUserNameString) || TextUtils.isEmpty(etPassWordString)) {
            Toast.makeText(this, "不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else {
            configEntity.ftpIp = (etUrlString);
            configEntity.ftpPort = Integer.parseInt(etPortString);
            configEntity.ftpUser = (etUserNameString);
            configEntity.ftpPassWord = (etPassWordString);
            ConfigUtils.saveftpConfig(context, configEntity);
        }
    }



}
