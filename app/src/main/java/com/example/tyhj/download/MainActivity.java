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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import entities.FileInfo;
import services.DownloadService;

public class MainActivity extends Activity {
    public static final String URL="http://www.imooc.com/mobile/imooc.apk";
    public static final String URL1="http://www.imooc.com/download/Activator.exe";
    public static final String URL2="http://www.imooc.com/download/iTunes64Setup.exe";
    private ListView lvFil=null;
    private List<FileInfo> mFilelist=null;
    private FileListAdpter mAdpter=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter=new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mRe,filter);
        lvFil= (ListView) findViewById(R.id.lvFile);
      //文件集合
        mFilelist=new ArrayList<FileInfo>();
        FileInfo fileInfo=new FileInfo(0,URL,"imooc.apk",0,0);
        FileInfo fileInfo1=new FileInfo(1,URL1,"Activator.exe",0,0);
        FileInfo fileInfo2=new FileInfo(2,URL2,"iTunes64Setup.exe",0,0);
        mFilelist.add(fileInfo);
        mFilelist.add(fileInfo1);
        mFilelist.add(fileInfo2);
        //创建适配器
        mAdpter=new FileListAdpter(this,mFilelist);
       lvFil.setAdapter(mAdpter);
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
                int id=intent.getIntExtra("id",0);
                mAdpter.updatePragress(id,finished);
                //下载完毕
            }else if(DownloadService.ACTION_FINISHED.equals(intent.getAction())){
                FileInfo fileInfo= (FileInfo) intent.getSerializableExtra("fileInfo");
                Toast.makeText(MainActivity.this,fileInfo.getFileName()+"下载完毕",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}
