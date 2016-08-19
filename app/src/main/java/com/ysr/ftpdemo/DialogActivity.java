package com.ysr.ftpdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ysr.ftpdemo.beans.FilesBean;
import com.ysr.ftpdemo.ftp.FtpUtils;
import com.ysr.ftpdemo.utils.ConfigEntity;
import com.ysr.ftpdemo.utils.ConfigUtils;
import com.ysr.ftpdemo.utils.UserConfig;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class DialogActivity extends Activity {
    private ListView mListView;
    private Context context;
    private WindowManager.LayoutParams p;
    private static int width = -1;
    private static int height = -1;
    private Button btnPost;
    private ConfigEntity configEntity;
    private ProgressDialog pd;
    private MyAdapters adapters;
    private static final String TAG = "FileShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        //设置对话框大小
        if (-1 == width) {
            width = UserConfig.getWidthPixels(this) * 2 / 3;
            height = UserConfig.getHeightPixels(this) * 2 / 3;
        }
        p = getWindow().getAttributes();
        p.width = width;
        p.height = height;
        Window win = this.getWindow();
        win.setAttributes(p);
        mListView = (ListView) findViewById(R.id.lvFile);
        btnPost = (Button) findViewById(R.id.btnPost);
        btnPost.setOnClickListener(MyPostFilelistener);
        context = this;
        //初始化数据
        configEntity = ConfigUtils.loadftpConfig(context);
        ScanFileTask sft = new ScanFileTask();
        sft.execute();


        //下载操作
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String fileName = adapters.getList().get(position).getTitle();
                File f = new File("/mnt/sdcard/download/" + fileName);
                //判断文件是否存在
                if (f.exists()) {
                    //打开文件
                    openFile(f);
                } else {
                    DownLoadFileTask dlft = new DownLoadFileTask();
                    dlft.execute(fileName);
                }


            }
        });
        //删除操作
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            //  private   View delView;
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = adapters.getList().get(position).getTitle();
                DelFileTask dft = new DelFileTask();
                dft.execute(fileName);
                return true;


            }
        });


    }

    /**
     * 异步网络扫描文件列表
     */

    public class ScanFileTask extends AsyncTask<Void, File, List<FilesBean>> {
        @Override
        protected List<FilesBean> doInBackground(Void... voids) {
            return getFileName();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            正在扫描
            pd = new ProgressDialog(DialogActivity.this);
            pd.setMessage("正在扫描文件...");
            pd.show();
        }

        @Override
        protected void onProgressUpdate(File... values) {
            super.onProgressUpdate(values);
            pd.setMessage(values[0].getName());
        }

        @Override
        protected void onPostExecute(List<FilesBean> filesBeen) {
            super.onPostExecute(filesBeen);
            adapters = new MyAdapters(DialogActivity.this, filesBeen);
            mListView.setAdapter(adapters);
            pd.dismiss();
        }

        public List<FilesBean> getFileName() {
            List<FilesBean> uploadFileNameList = new ArrayList<FilesBean>();

            FTPClient ftp = new FTPClient();
            //解码不然中文乱码
            ftp.setControlEncoding("utf-8");
            try {

                int reply;
                ftp.connect(configEntity.ftpIp, configEntity.ftpPort);//连接FTP服务器
                //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
                ftp.login(configEntity.ftpUser, configEntity.ftpPassWord);//登录
                ftp.enterLocalPassiveMode();
                reply = ftp.getReplyCode();
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                }
                FTPFile[] files = ftp.listFiles("");
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        Date date = new Date(files[i].getTimestamp().getTime().getTime());
                        FilesBean file = new FilesBean();
                        file.setTitle(files[i].getName());
                        file.setTime(date + "");
                        uploadFileNameList.add(file);
                        Log.i("FileShareActivity", "文件名：" + files[i].getName());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return uploadFileNameList;
        }
    }

    /**
     * 异步网络上传文件
     */
    public class UpLoadFileTask extends AsyncTask<String, String, String> {
        //预处理
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(DialogActivity.this);
            pd.setMessage("正在上传文件...");
            pd.show();

        }

        @Override
        protected String doInBackground(String... params) {

            final String aafileurl = params[0];
            // 上传
            File file = new File(aafileurl);
            try {

                //单文件上传
                new FtpUtils(configEntity.ftpIp, configEntity.ftpPort, configEntity.ftpUser, configEntity.ftpPassWord).uploadSingleFile(file, "", new FtpUtils.UploadProgressListener() {
                    @Override
                    public void onUploadProgress(String currentStep, long uploadSize, File file) {
                        // TODO Auto-generated method stub
                        Log.d("FtpUtils", currentStep);
                        if (currentStep.equals(MainActivity.FTP_UPLOAD_SUCCESS)) {
                            //子线程使用toast
                            Message msg = msgHandler.obtainMessage();
                            msg.arg1 = R.string.post_success;
                            msgHandler.sendMessage(msg);
                            Log.d(TAG, "-----shangchuan---SUCCESS----");
                        } else if (currentStep.equals(MainActivity.FTP_UPLOAD_LOADING)) {
                            long fize = file.length();
                            float num = (float) uploadSize / (float) fize;
                            int result = (int) (num * 100);
                            Log.d(TAG, "-----shangchuan---" + result + "%");
                        }
                    }
                });
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return null;
        }

        //结束时线程
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            pd = null;
            ScanFileTask sft = new ScanFileTask();
            sft.execute();
            adapters.notifyDataSetChanged();
        }
    }


    /**
     * 异步网络下载文件
     */

    public class DownLoadFileTask extends AsyncTask<String, String, String> {
        //预处理
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(DialogActivity.this);
            pd.setMessage("正在下载文件...");
            pd.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String downFileName = params[0];
            try {

                //单文件下载
                new FtpUtils(configEntity.ftpIp, configEntity.ftpPort, configEntity.ftpUser, configEntity.ftpPassWord).downloadSingleFile(downFileName, "/mnt/sdcard/download/", downFileName, new FtpUtils.DownLoadProgressListener() {

                    @Override
                    public void onDownLoadProgress(String currentStep, long downProcess, File file) {
                        Log.d(TAG, currentStep);
                        if (currentStep.equals(MainActivity.FTP_DOWN_SUCCESS)) {
                            Log.d(TAG, "-----xiazai--successful");
                        } else if (currentStep.equals(MainActivity.FTP_DOWN_LOADING)) {
                            Log.d(TAG, "-----xiazai---" + downProcess + "%");
                        }
                    }

                });

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return null;
        }

        //结束时线程
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            pd = null;
        }


    }

    /**
     * 异步网络删除文件
     */

    public class DelFileTask extends AsyncTask<String, String, String> {
        //预处理
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(DialogActivity.this);
            pd.setMessage("正在删除文件...");
            pd.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String delFileName = params[0];
            // 删除
            try {

                new FtpUtils(configEntity.ftpIp, configEntity.ftpPort, configEntity.ftpUser, configEntity.ftpPassWord).deleteSingleFile(delFileName, new FtpUtils.DeleteFileProgressListener() {

                    @Override
                    public void onDeleteProgress(String currentStep) {
                        Log.d(TAG, currentStep);
                        if (currentStep.equals(MainActivity.FTP_DELETEFILE_SUCCESS)) {
                            Log.d(TAG, "-----shanchu--success");
                        } else if (currentStep.equals(MainActivity.FTP_DELETEFILE_FAIL)) {
                            Log.d(TAG, "-----shanchu--fail");
                        }
                    }

                });

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return null;
        }

        //结束时线程
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            pd = null;
            ScanFileTask sft = new ScanFileTask();
            sft.execute();
            adapters.notifyDataSetChanged();
        }


    }

    /**
     * 打开文件管理器
     */
    View.OnClickListener MyPostFilelistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "打开文件管理器"), MainActivity.FILE_SELECT_CODDE);
        }
    };

    /**
     * handler发送消息
     */

    private final Handler msgHandler = new Handler() {
        public void handleMessage(Message msg) {

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

    /**
     * 文件列表adapter
     */
    class MyAdapters extends BaseAdapter {
        //private Context context;
        private List<FilesBean> list;
        private LayoutInflater mInflater;

        public MyAdapters(Context context, List<FilesBean> list) {
//            this.context = context;
            mInflater = LayoutInflater.from(context);
            this.list = list;
        }

        public List<FilesBean> getList() {
            return list;
        }

        public void setList(List<FilesBean> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            //return list.get(position);
            return null != list ? list.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (v == null) {
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.item, viewGroup, false);
                holder.title = (TextView) v.findViewById(R.id.ItemTitle);
                holder.description = (TextView) v.findViewById(R.id.ItemText);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            holder.title.setText(list.get(position).getTitle());
            holder.description.setText(list.get(position).getTime());
            return v;
        }

        class ViewHolder {
            public TextView title;
            public TextView description;
        }
    }

    /**
     * 接收文件路径
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.FILE_SELECT_CODDE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = uri.getPath();
            Log.e("path", path);
            UpLoadFileTask uft = new UpLoadFileTask();
            uft.execute(path);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 关闭对话框
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }


    /**
     * 打开文件
     *
     * @param file
     */
    private void openFile(File file) {
        //Uri uri = Uri.parse("file://"+file.getAbsolutePath());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(Uri.fromFile(file), type);
        //跳转
        startActivity(intent);
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param file
     */
    private String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
    /* 获取文件的后缀名 */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    //建立一个MIME类型与文件后缀名的匹配表
    private final String[][] MIME_MapTable = {
            //{后缀名，    MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "video/mp4"},
            {".wps", "application/vnd.ms-works"},
            //{".xml",    "text/xml"},
            {".xml", "text/plain"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {".rtf", "application/msword"},
            {".f4v", "video/mp4"},
            {"", "*/*"}
    };

}
