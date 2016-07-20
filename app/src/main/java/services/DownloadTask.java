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
import java.util.List;

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
    public DownloadTask(Context mContext,FileInfo mFileInfo){
        this.mContext=mContext;
        this.mFileInfo=mFileInfo;
        mDao=new ThreadDAOImpl(mContext);
    }



    public void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos= mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo=null;
        if(threadInfos.size()==0){
            //初始化线程信息对象
            threadInfo=new ThreadInfo(0,mFileInfo.getUrl(),0,mFileInfo.getLength(),0);
        }else {
            threadInfo=threadInfos.get(0);
            //mFileInfo.setFinished(threadInfo.getFinished());  //没有这步，mFileInfo的finished就是0
        }
        //创建子线程进行下载
        new DownLoadThread(threadInfo).start();
    }
    /*
    * 下载线程
    */
    class DownLoadThread extends Thread{
        private ThreadInfo mThreadInfo=null;
        HttpURLConnection connection=null;
        InputStream input=null;
        RandomAccessFile raf=null;
        public DownLoadThread(ThreadInfo mThreadInfo){
            this.mThreadInfo=mThreadInfo;
        }

        @Override
        public void run() {
            //向数据库插入线程信息
            if(!mDao.isExists(mThreadInfo.getUrl(),mThreadInfo.getId())){
                mDao.insertThread(mThreadInfo);
            }
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
                        //把下载进度发送到Activity
                        mfinished=mfinished+len;
                        if(System.currentTimeMillis()-time>500) {
                            time=System.currentTimeMillis();
                            intent.putExtra("finished", mfinished * 100 / mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                            Log.i("第一",mfinished * 100 / mFileInfo.getLength()+"mfinished:"+mfinished+"总的："+mFileInfo.getLength());
                        }
                        //下载暂停，保存进度
                        if(isPause){
                            mDao.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mfinished);
                            return;
                        }
                    }
                    intent.putExtra("finished", 100);
                    mContext.sendBroadcast(intent);
                    //删除线程信息
                    mDao.deleteThread(mThreadInfo.getUrl(),mThreadInfo.getId());
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
}
