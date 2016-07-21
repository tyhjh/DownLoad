package services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import entities.FileInfo;

public class DownloadService extends Service {
    public static final int MSG_INIT=0;
    public static final String ACTION_STRAT="ACTION_STRAT";
    public static final String ACTION_STOP="ACTION_STOP";
    public static final String ACTION_UPDATE="ACTION_UPDATE";
    public static final String ACTION_FINISHED="ACTION_FINISHED";
    public static final String DOWNLOAD_PATH= Environment
            .getExternalStorageDirectory().getAbsolutePath()+"/Adownloads/";
    private InitThread mInitThread=null;
    //下载任务集合
    private Map<Integer,DownloadTask> mTasks=
            new LinkedHashMap<Integer, DownloadTask>();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_STRAT.equals(intent.getAction())){
            FileInfo fileInfo= (FileInfo) intent.getSerializableExtra("fileInfo");
            InitThread initThread=new InitThread(fileInfo);
            DownloadTask.sExecutorService.execute(initThread);
        }else  if(ACTION_STOP.equals(intent.getAction())){
            //暂停下载
            FileInfo fileInfo= (FileInfo) intent.getSerializableExtra("fileInfo");
            DownloadTask task=mTasks.get(fileInfo.getId());
            if(task!=null){
                //停止下载
                task.isPause=true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


     Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo= (FileInfo) msg.obj;
                    Log.i("test","init:"+fileInfo.toString());
                    //启动下载任务
                    DownloadTask task=new DownloadTask(DownloadService.this,fileInfo,3);
                    task.download();
                    //下载任务添加到集合中
                    mTasks.put(fileInfo.getId(),task);
                    break;
            }
        }
    };
    /*
    * 初始化子线程
    *
    */
    class InitThread extends Thread{
        private FileInfo fileInfo;
        public InitThread(FileInfo fileInfo){
            this.fileInfo=fileInfo;
        }

        @Override
        public void run() {
            RandomAccessFile raf=null;
            HttpURLConnection connection=null;
            try {
                URL url=new URL(fileInfo.getUrl());
                connection= (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int  length=-1;
                if(connection.getResponseCode()== HttpStatus.SC_OK){
                    //获取文件长度
                    length=connection.getContentLength();
                }
                if(length<=0){
                    return;
                }
                File dir=new File(DOWNLOAD_PATH);
                if(!dir.exists()){
                    dir.mkdir();
                }
                //在本地创建文件
                File file=new File(dir,fileInfo.getFileName());
                raf=new RandomAccessFile(file,"rwd");
                //设置文件长度
                raf.setLength(length);
                fileInfo.setLength(length);
                handler.obtainMessage(MSG_INIT,fileInfo).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    connection.disconnect();
                    raf.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
