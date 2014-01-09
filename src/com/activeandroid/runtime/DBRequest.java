package com.activeandroid.runtime;

import java.util.UUID;

/**
 * Created by andrewgrosner
 * Date: 12/11/13
 * Contributors:
 * Description: The basic request object that's placed on the DBRequestQueue for processing.
 * The {@link com.activeandroid.runtime.DBRequestQueue} uses a priority queue that will process
 * this class based on the priority assigned to it.
 *
 * There are four main kinds of requests:
 *  For requests that require UI or immediate retrieval, use PRIORITY_UI
 *  For requests that are displayed in the UI some point in the near future, use PRIORITY_HIGH
 *  For the bulk of data requests, use PRIORITY_NORMAL
 *  For any request that's non-essential use PRIORITY_LOW
 */
public abstract class DBRequest implements Comparable<DBRequest> {

    /**
     * Low priority requests, reserved for non-essential tasks
     */
    public static int PRIORITY_LOW = 0;

    /**
     * The main of the requests, good for when adding a bunch of
     * data to the DB that the app does not access right away.
     */
    public static int PRIORITY_NORMAL = 1;

    /**
     * Reserved for tasks that will influence user interaction, such as displaying data in the UI
     * some point in the future (not necessarily right away)
     */
    public static int PRIORITY_HIGH = 2;

    /**
     * Reserved for only immediate tasks and all forms of fetching that will display on the UI
     */
    public static int PRIORITY_UI = 5;

    public abstract void run();

    /**
     * The higher the number, the faster the request will be processed,
     * default is PRIORITY_LOW
     */
    private int priority = PRIORITY_LOW;

    /**
     * Give this request a name
     */
    private String name;

    /**
     * Constructs a new instance specifying a priority and name
     * @param priority
     * @param name
     */
    public DBRequest(int priority, String name){
        this.priority = priority;
        this.name = name;
    }

    /**
     * Constructs a new instance with a priority and UUID for the name
     * @param priority
     */
    public DBRequest(int priority){
        this.priority = priority;
        this.name = UUID.randomUUID().toString();
    }

    /**
     * Creates a new, low priority request
     */
    public DBRequest(){
        this.name = UUID.randomUUID().toString();
    }

    @Override
    public int compareTo(DBRequest another) {
        return another.priority - priority;
    }

    public String getName() {
        return name;
    }
}
