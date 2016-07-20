package com.example.tyhj.download;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;

import entities.FileInfo;
import services.DownloadService;

public class MainActivity extends Activity {
    public static final String URL="http://www.imooc.com/mobile/imooc.apk";
    public static final String URL1="ftp://ygdy8:ygdy8@y153.dydytt.net:8145/[%E9%98%B3%E5%85%89%E7%94%B5%E5%BD%B1www.ygdy8.com].%E7%8B%99%E5%87%BB%E6%89%8B%EF%BC%9A%E7%89%B9%E5%88%AB%E8%A1%8C%E5%8A%A8.BD.720p.%E4%B8%AD%E8%8B%B1%E5%8F%8C%E5%AD%97%E5%B9%95.rmvb";
    FileInfo fileInfo;
    Button btdown,btpause;
    ProgressBar pbProgressbar;
    TextView textView;

    void start(){
        Intent intent=new Intent(MainActivity.this, DownloadService.class);
        intent.setAction(DownloadService.ACTION_STRAT);
        intent.putExtra("fileInfo", fileInfo);
        startService(intent);
    }
    void pause(){
        Intent intent=new Intent(MainActivity.this, DownloadService.class);
        intent.setAction(DownloadService.ACTION_STOP);
        intent.putExtra("fileInfo", fileInfo);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter=new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mRe,filter);
        textView= (TextView) findViewById(R.id.tvname);
        btdown= (Button) findViewById(R.id.btdown);
        btpause= (Button) findViewById(R.id.btpause);
        pbProgressbar= (ProgressBar) findViewById(R.id.pbProgressbar);
        pbProgressbar.setMax(100);
        fileInfo=new FileInfo(0,URL,"imooc.apk",0,0);
        btdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        btpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRe);
    }

    BroadcastReceiver mRe=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DownloadService.ACTION_UPDATE.equals(intent.getAction())){
                int finished=intent.getIntExtra("finished",0);
                pbProgressbar.setProgress(finished);
            }
        }
    };


}
