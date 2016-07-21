package com.example.tyhj.download;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import entities.FileInfo;
import services.DownloadService;

/**
 * Created by _Tyhj on 2016/7/21.
 */
public class FileListAdpter extends BaseAdapter {

    private Context mContext=null;
    private List<FileInfo> mFileList=null;

    public FileListAdpter(Context mContext, List<FileInfo> mFileList) {
        this.mContext = mContext;
        this.mFileList = mFileList;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder=null;
        final FileInfo fileInfo=mFileList.get(position);
        if(view==null){
            //加载试图
            view=LayoutInflater.from(mContext).inflate(R.layout.listitem,null);
            //获取控件
            viewHolder=new ViewHolder();
            viewHolder.tvFile= (TextView) view.findViewById(R.id.tvname);
            viewHolder.btstart= (Button) view.findViewById(R.id.btdown);
            viewHolder.btstop= (Button) view.findViewById(R.id.btpause);
            viewHolder.progressBar= (ProgressBar) view.findViewById(R.id.pbProgressbar);
            //设置控件
            viewHolder.tvFile.setText(fileInfo.getFileName());
            viewHolder.progressBar.setMax(100);
            viewHolder.btstart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_STRAT);
                    intent.putExtra("fileInfo", fileInfo);
                    mContext.startService(intent);
                }
            });
            viewHolder.btstop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_STOP);
                    intent.putExtra("fileInfo", fileInfo);
                    mContext.startService(intent);
                }
            });
            view.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.progressBar.setProgress(fileInfo.getFinished());
        return view;
    }

    /*
    * 更新进度条
    *
    */
    public void updatePragress(int id,int progress){
        FileInfo fileInfo=mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    static class ViewHolder{
        TextView tvFile;
        Button btstart,btstop;
        ProgressBar progressBar;
    }
}
