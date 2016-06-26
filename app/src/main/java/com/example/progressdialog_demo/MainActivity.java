package com.example.progressdialog_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Button mBtn;
    private TextView mTv;
    private ProgressDialog mProgressDialog;

    /**
     * 网络请求的总次数
     */
    private static int sRequestCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBtn = (Button) findViewById(R.id.btn);
        mTv = (TextView) findViewById(R.id.tv);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadDataTask().execute();
            }
        });
    }

    /**
     * 模拟网络请求的异步任务.
     */
    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("加载中...");
                mProgressDialog.setCancelable(true);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        LoadDataTask.this.cancel(false);
                    }
                });
            }
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // 模拟网络请求的耗时
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTv.setText("这是第 " + (++sRequestCount) + " 次加载");
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            // TODO: 这里做取消网络请求的操作.
            dismissProgressDialog();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sRequestCount = 0;
    }
}
