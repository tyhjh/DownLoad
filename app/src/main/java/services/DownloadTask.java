package services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import entities.FileInfo;
import entities.ThreadInfo;
import mydb.ThreadDAO;
import mydb.ThreadDAOImpl;

/**
 * Created by _Tyhj on 2016/7/20.
 * 下载任务
 */
public class DownloadTask {
    private Context mContext=null;
    private FileInfo mFileInfo=null;
    public boolean isPause=false;
    private int mfinished=0;
    private ThreadDAOImpl mDao;
    private int mThreadCount=1;//线程数量
    private List<DownLoadThread> mThreadList=null;
    //线程池
    public static ExecutorService sExecutorService=
            Executors.newCachedThreadPool();


    public DownloadTask(Context mContext,FileInfo mFileInfo,int mThreadCount){
        this.mContext=mContext;
        this.mFileInfo=mFileInfo;
        this.mThreadCount=mThreadCount;
        mDao=new ThreadDAOImpl(mContext);
    }



    public void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos= mDao.getThreads(mFileInfo.getUrl());
        if(threadInfos.size()==0){
            //获得每个线程下载的长度
            int length=mFileInfo.getLength()/mThreadCount;
            for(int i=0;i<mThreadCount;i++){
                //创建线程信息
                ThreadInfo threadInfo=new ThreadInfo(i,mFileInfo.getUrl(),length*i,length*(i+1)-1,0);
                if(i==mThreadCount-1){
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                //添加线程信息到集合
                threadInfos.add(threadInfo);
                //向数据库插入线程信息
                mDao.insertThread(threadInfo);
            }
        }
        mThreadList=new ArrayList<DownLoadThread>();
        //启动多个线程进行下载
        for(ThreadInfo info : threadInfos){
            DownLoadThread thread=new DownLoadThread(info);
            //thread.start();
            DownloadTask.sExecutorService.execute(thread);
            //添加线程到集合
            mThreadList.add(thread);
        }
    }
    //是否所有线程都完成下载
    private synchronized void cheackAllThreadFinished(){
        boolean allFinished=true;
        for(DownLoadThread thread : mThreadList){
            if(!thread.isFinished){
                allFinished=false;
                break;
            }
        }
        if(allFinished){
            //删除线程信息
            // mDao.deleteThread(mFileInfo.getUrl());
            Intent intent=new Intent(DownloadService.ACTION_UPDATE);
            intent.putExtra("finished", 100);
            intent.putExtra("id", mFileInfo.getId());
            mContext.sendBroadcast(intent);


            Intent intent2=new Intent(DownloadService.ACTION_FINISHED);
            intent2.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent2);
        }
    }
    /*
    * 下载线程
    */
    class DownLoadThread extends Thread{
        private ThreadInfo mThreadInfo=null;
        public boolean isFinished=false;//线程是否结束
        HttpURLConnection connection=null;
        InputStream input=null;
        RandomAccessFile raf=null;
        public DownLoadThread(ThreadInfo mThreadInfo){
            this.mThreadInfo=mThreadInfo;
        }

        @Override
        public void run() {
            try {
                URL url=new URL(mThreadInfo.getUrl());
                connection= (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                //设置下载位置
                int start=mThreadInfo.getStart()+mThreadInfo.getFinished();
                connection.setRequestProperty("Range","bytes="+start+"-"+mThreadInfo.getEnd());
                //设置写入位置
                File file=new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName());
                raf=new RandomAccessFile(file,"rwd");
                raf.seek(start);
                Intent intent=new Intent(DownloadService.ACTION_UPDATE);
                //开始下载
                mfinished+=mThreadInfo.getFinished();
                if(connection.getResponseCode()== HttpStatus.SC_PARTIAL_CONTENT){
                    //读取数据
                    input=connection.getInputStream();
                    byte[] buffer=new byte[1024*4];
                    int len=-1;
                    long time=System.currentTimeMillis();
                    while ((len=input.read(buffer))!=-1){
                        //写入文件
                        raf.write(buffer,0,len);
                        //把总下载进度发送到Activity
                        addFinished(len);
                        //每个线程的下载进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished()+len);
                        if(System.currentTimeMillis()-time>700) {
                            time=System.currentTimeMillis();
                            intent.putExtra("finished", (mfinished/1042 * 100) / (mFileInfo.getLength()/1024));
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                            Log.i("第一","进度"+(mfinished /100* 100) / (mFileInfo.getLength()/100)+"......."+"长度："+len+".........."+"完成"+mfinished+"........."+"总的"+mFileInfo.getLength());
                        }
                        //下载暂停，保存进度
                        if(isPause){
                            mDao.updateThread(mThreadInfo.getUrl(),
                                    mThreadInfo.getId(),
                                    mThreadInfo.getFinished());
                            return;
                        }
                    }
                   isFinished=true;
                    //检查全部线程是否下载完成
                    cheackAllThreadFinished();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    connection.disconnect();
                    raf.close();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized void addFinished(int len) {
        mfinished=mfinished+len;
    }
}
