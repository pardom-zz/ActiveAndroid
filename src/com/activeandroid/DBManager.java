package com.activeandroid;

import android.database.DatabaseUtils;
import android.os.Handler;

import com.activeandroid.query.Select;
import com.activeandroid.receiver.CollectionReceiver;
import com.activeandroid.receiver.ObjectReceiver;
import com.activeandroid.runtime.DBRequest;
import com.activeandroid.runtime.DBRequestQueue;
import com.activeandroid.util.SQLiteUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewgrosner
 * Date: 11/12/13
 * Contributors:
 * Description: Provides a handy base implementation for adding and getting objects from the database.
 *
 * @param <OBJECT_CLASS> - the class of objects that represent a Model from the DB
 */
public abstract class DBManager<OBJECT_CLASS extends Model> {

    protected Class<OBJECT_CLASS> mObjectClass;

    /**
     * Runs all of the UI threaded requests
     */
    protected Handler mRequestHandler = new Handler();

    /**
     * Constructs a new instance while keeping an instance of the class for its objects
     * @param classClass
     */
    public DBManager(Class<OBJECT_CLASS> classClass){
        mObjectClass = classClass;
    }

    /**
     * Override this method to have one instance of the manager accross the app
     * @return
     */
    public static DBManager getSharedInstance(){
        throw new IllegalStateException("Cannot call the base implementation of this method");
    }

    /**
     * Runs a request from the DB in the request queue
     * @param runnable
     */
    protected void processOnBackground(DBRequest runnable){
       DBRequestQueue.getSharedInstance().add(runnable);
    }

    /**
     * Runs UI operations in the handler
     * @param runnable
     */
    protected synchronized void processOnForeground(Runnable runnable){
        mRequestHandler.post(runnable);
    }

    /**
     * Adds an object to the manager's database
     * @param inObject - object of the class defined by the manager
     */
    public OBJECT_CLASS add(OBJECT_CLASS inObject){
        try{
            if(inObject.exists()){
                inObject.delete();
            }
        }catch (NullPointerException n){

        }
        inObject.save();
        return inObject;
    }

    /**
     * Adds a json object to this class, however its advised you ensure that the jsonobject being passed is what you want, since there's no type checking
     * @param object
     */
    public OBJECT_CLASS add(JSONObject object){
        try {
            return add(mObjectClass.getConstructor(JSONObject.class).newInstance(object));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds an object to the DB in the BG
     * @param jsonObject
     * @param objectReceiver
     * @param priority
     */
    public void addInBackground(final JSONObject jsonObject, final ObjectReceiver<OBJECT_CLASS> objectReceiver, final int priority){
        processOnBackground(new DBRequest(priority, "add") {
            @Override
            public void run() {
                final OBJECT_CLASS object = add(jsonObject);
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        objectReceiver.onObjectReceived(object);
                    }
                });
            }
        });
    }

    /**
     * Adds an object to the DB in the BG
     * @param objectReceiver
     * @param priority
     */
    public void addInBackground(final OBJECT_CLASS inObject, final ObjectReceiver<OBJECT_CLASS> objectReceiver, final int priority){
        processOnBackground(new DBRequest(priority, "add") {
            @Override
            public void run() {
                final OBJECT_CLASS object = add(inObject);
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        objectReceiver.onObjectReceived(object);
                    }
                });
            }
        });
    }

    /**
     * Adds all objects to the DB
     * @param objects
     */
    public void addAll(ArrayList<OBJECT_CLASS> objects){
        ActiveAndroid.beginTransaction();
        try{
            for(OBJECT_CLASS object: objects){
                add(object);
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * Adds all objects from the passed jsonarray, may NOT be type-safe so be careful with this
     * @param array
     */
    public void addAll(JSONArray array){
        ActiveAndroid.beginTransaction();
        try{
            for(int i = 0; i < array.length();i++){
                OBJECT_CLASS object = mObjectClass.getConstructor(JSONObject.class).newInstance(array.get(i));
                add(object);
            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            ActiveAndroid.endTransaction();
        }

    }

    public void addAllInBackground(final JSONArray array, final Runnable finishedRunnable, String tag, int priority){
        processOnBackground(new DBRequest(priority, "add "+ tag) {
            @Override
            public void run() {
                addAll(array);

                if(finishedRunnable!=null)
                    processOnForeground(finishedRunnable);
            }
        });
    }

    public void addAllInBackground(final JSONArray array, final Runnable finishedRunnable, String tag){
        addAllInBackground(array, finishedRunnable, tag, DBRequest.PRIORITY_LOW);
    }

    /**
     * Retrieves a list of objects from the database without any threading
     * Its recommended not to call this method in the foreground thread
     * @return
     */
    public List<OBJECT_CLASS> getAll(){
        return new Select().from(mObjectClass).execute();
    }

    /**
     * Retrieves a list of objects from the database without any threading with the sort passed
     * Its recommended not to call this method in the foreground thread
     * @param sort - valid SQLLite syntax for sort e.g. name ASC
     * @return
     */
    public List<OBJECT_CLASS> getAllWithSort(String sort){
        return new Select().from(mObjectClass).orderBy(sort).execute();
    }

    /**
     * Fetches objects from this DB on the BG
     * @param receiver - function to call when finished that passes the list of objects that was found
     */
    public void fetchAll(final CollectionReceiver<OBJECT_CLASS> receiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI, "fetch") {
            @Override
            public void run() {
                final List<OBJECT_CLASS> list = getAll();
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        receiver.onCollectionReceived(list);
                    }
                });
            }
        });
    }

    /**
     * Fetches objects from this DB on the BG calling orderBy with the sort passed.
     * @param sort - valid SQLLite syntax for sort e.g. name ASC
     * @param receiver - function to call when finished that passes the list of objects that was found
     */
    public void fetchAllWithSort(final String sort, final CollectionReceiver<OBJECT_CLASS> receiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI, "fetch") {
            @Override
            public void run() {
                final List<OBJECT_CLASS> list = getAllWithSort(sort);
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        receiver.onCollectionReceived(list);
                    }
                });
            }
        });
    };

    public void fetchAllWithColumnValue(final Object value, final String column, final CollectionReceiver<OBJECT_CLASS> receiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI, "fetch") {
            @Override
            public void run() {
                final List<OBJECT_CLASS> list = getAllWithColumnValue(column, value);
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        receiver.onCollectionReceived(list);
                    }
                });
            }
        });
    }

    /**
     * This will get the where statement for this object, the amount of ids passed must match the primary key column size
     * @param uid
     * @return
     */
    public OBJECT_CLASS getObjectById(Object...ids){
        return new Select().from(mObjectClass).where(SQLiteUtils.getWhereStatement(mObjectClass, Cache.getTableInfo(mObjectClass)), ids).executeSingle();
    }

    /**
     * Returns a single object with the specified column value.
     * Useful for getting objects with a specific primary key
     * @param column
     * @param uid
     * @return
     */
    public OBJECT_CLASS getObjectByColumnValue(String column, Object uid){
        return new Select().from(mObjectClass).where(column+" =?", uid).executeSingle();
    }

    /**
     * Returns all objects with the specified column value
     * @param column
     * @param value
     * @return
     */
    public List<OBJECT_CLASS> getAllWithColumnValue(String column, Object value){
        return new Select().from(mObjectClass).where(column + "= ?", value).execute();
    }

    /**
     * Returns the count of rows from this DB manager's DB
     * @return
     */
    public long getCount(){
        return DatabaseUtils.queryNumEntries(Cache.openDatabase(), Cache.getTableName(mObjectClass));
    }

    /**
     * Fetches the count on the DB thread and returns it on the handler
     * @param objectReceiver
     */
    public void fetchCount(final ObjectReceiver<Long> objectReceiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI) {
            @Override
            public void run() {
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        objectReceiver.onObjectReceived(getCount());
                    }
                });
            }
        });
    }

    /**
     * Will return the object if its within the DB, if not, it will call upon an object requester to get the data from the API
     *
     * @param objectReceiver
     * @param uid
     * @return true if the object exists in the DB, otherwise its on a BG thread
     */
    public boolean fetchObject(final ObjectReceiver<OBJECT_CLASS> objectReceiver, final Object... uid){
        OBJECT_CLASS object = getObjectById(uid);
        if(object==null){
            processOnForeground(new Runnable() {
                @Override
                public void run() {
                    requestObject(objectReceiver);
                }
            });
            return false;
        } else{
            objectReceiver.onObjectReceived(object);
            return true;
        }
    }

    /**
     * Implement this method to perform a request if the object does not exist in the DB
     * @param objectReceiver
     * @param uid
     */
    public abstract void requestObject(final ObjectReceiver<OBJECT_CLASS> objectReceiver, final Object... uid);

    public Class<OBJECT_CLASS> getObjectClass() {
        return mObjectClass;
    }
}
