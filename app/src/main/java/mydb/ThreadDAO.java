package mydb;

import java.util.List;

import entities.ThreadInfo;

/**
 * Created by _Tyhj on 2016/7/20.
 * 数据访问接口
 */
public interface ThreadDAO {
    //插入
    public void insertThread(ThreadInfo threadInfo);
    //删除线程
    public void deleteThread(String url);
    //更新线程
    public void updateThread(String url,int thread_id,int finished);
    //查询线程
    public List<ThreadInfo> getThreads(String url);
    //线程是否存在
    public boolean isExists(String url,int thread_id);
}
