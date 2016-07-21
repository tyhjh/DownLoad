package mydb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by _Tyhj on 2016/7/20.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper shelp=null;
    private static final String DB_NAME="download.sb";
    private static final int VERSION=1;
    private static final String SQL_CREATE="create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer,url text,start integer,end integer,finished integer)";
    private static final String SQL_DROP="drop table if exists thread_info";

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }
    /*
    * 获得类对象
    */
public static  DBHelper getInstance(Context context){
    if(shelp==null){
        shelp=new DBHelper(context);
    }
    return shelp;
}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }
}
