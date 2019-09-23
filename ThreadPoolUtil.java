package cn.liuboyi.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description: 线程池工具类
 *
 * @author: boyi.liu
 * Date: 2019/9/23 10:17
 */
public class ThreadPoolUtil {

    private ExecutorService pool;

    private static class SingletonHolder {
        private static ThreadPoolUtil instance = new ThreadPoolUtil();
    }

    public static ThreadPoolUtil getInstance(){
        return SingletonHolder.instance;
    }

    private ThreadPoolUtil() {
        int cpu = Runtime.getRuntime().availableProcessors();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("operation-log-pool-%d").build();
        pool = new ThreadPoolExecutor(5, cpu * 5,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(64), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static void main(String[] args) {
        ThreadPoolUtil.getInstance().pool.execute(()-> System.out.println("第1条消息"));
        ThreadPoolUtil.getInstance().pool.execute(()-> System.out.println("第2条消息"));
        ThreadPoolUtil.getInstance().pool.execute(()-> System.out.println("第3条消息"));
        ThreadPoolUtil.getInstance().pool.execute(()-> System.out.println("第4条消息"));
        ThreadPoolUtil.getInstance().pool.execute(()-> System.out.println("第5条消息"));
        ThreadPoolUtil.getInstance().pool.execute(()-> System.out.println("第6条消息"));
        ThreadPoolUtil.getInstance().pool.shutdown();
    }

}
