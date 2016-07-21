package mydb;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import entities.ThreadInfo;

/**
 * Created by _Tyhj on 2016/7/20.
 * 数据访问接口实现
 */
public class ThreadDAOImpl implements ThreadDAO {

    private  DBHelper mHelper=null;

    public ThreadDAOImpl(Context context){
        mHelper=DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db=mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(),threadInfo.getUrl(),threadInfo.getStart(),threadInfo.getEnd(),threadInfo.getFinished()});
                db.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db=mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url=?",new Object[]{url});
        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db=mHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished=? where thread_id=?and url=?",new Object[]{finished,thread_id,url});
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db=mHelper.getReadableDatabase();
        List<ThreadInfo> list=new ArrayList<ThreadInfo>();
        Cursor cursor=
                db.rawQuery("select * from thread_info where url=?",new String[]{url});
        while (cursor.moveToNext()){
            ThreadInfo threadInfo=new ThreadInfo();
            threadInfo.setId(cursor.getInt(1));
            threadInfo.setUrl(cursor.getString(2));
            threadInfo.setStart(cursor.getInt(3));
            threadInfo.setEnd(cursor.getInt(4));
            threadInfo.setFinished(cursor.getInt(5));
            list.add(threadInfo);
        }
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db=mHelper.getReadableDatabase();
        Cursor cursor=
        db.rawQuery("select * from thread_info where url=? and thread_id=?",new String[]{url,thread_id+""});
        boolean exists=cursor.moveToNext();
        return exists;
    }
}
