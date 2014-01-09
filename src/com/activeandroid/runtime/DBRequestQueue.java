package com.activeandroid.runtime;

import android.os.Looper;
import android.os.Process;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by andrewgrosner
 * Date: 12/11/13
 * Contributors:
 * Description: will handle concurrent requests to the DB based on priority
 */
public class DBRequestQueue extends Thread{

    private static DBRequestQueue shared;

    /**
     * Queue of requests
     */
    private final PriorityBlockingQueue<DBRequest> mQueue;

    private boolean mQuit = false;

    /**
     * Gets and starts the request queue if it hasn't started yet.
     * @return
     */
    public static DBRequestQueue getSharedInstance(){
        if(shared==null){
            shared = new DBRequestQueue("DBRequestQueue");
            shared.start();
        }
        return shared;
    }

    /**
     * Creates a queue with the specified name to ID it.
     * @param name
     */
    public DBRequestQueue(String name) {
        super(name);

        mQueue = new PriorityBlockingQueue<DBRequest>();
    }

    @Override
    public void run() {
        Looper.prepare();
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        DBRequest runnable;
        while (true){
            try{
                runnable = mQueue.take();
            } catch (InterruptedException e){
                if(mQuit){
                    return;
                }
                continue;
            }

            try{
                runnable.run();
            } catch (Throwable t){
                throw new RuntimeException(t);
            }
        }

    }

    public void add(DBRequest runnable){
        synchronized (mQueue){
            mQueue.add(runnable);
        }
    }

    /**
     * Cancels the specified request.
     * @param runnable
     */
    public void cancel(DBRequest runnable){
        synchronized (mQueue){
            if(mQueue.contains(runnable)){
                mQueue.remove(runnable);
            }
        }
    }

    /**
     * Quits this process
     */
    public void quit(){
        mQuit = true;
        interrupt();
    }
}
