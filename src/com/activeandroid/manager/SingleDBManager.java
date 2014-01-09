package com.activeandroid.manager;

import android.database.DatabaseUtils;
import android.os.Handler;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.interfaces.ObjectRequester;
import com.activeandroid.query.Select;
import com.activeandroid.interfaces.CollectionReceiver;
import com.activeandroid.interfaces.ObjectReceiver;
import com.activeandroid.runtime.DBRequest;
import com.activeandroid.runtime.DBRequestQueue;
import com.activeandroid.util.SQLiteUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewgrosner
 * Date: 12/26/13
 * Contributors:
 * Description: This class will provide one instance for all tables,
 * however the downside requires the class of an object when retrieving from the DB.
 */
public class SingleDBManager {

    private static SingleDBManager manager;

    public static SingleDBManager getSharedInstance(){
        if(manager==null){
           manager = new SingleDBManager();
        }
        return manager;
    }

    /**
     * Runs all of the UI threaded requests
     */
    protected Handler mRequestHandler = new Handler();

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
    public <OBJECT_CLASS extends Model> OBJECT_CLASS add(OBJECT_CLASS inObject){
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
    public <OBJECT_CLASS extends Model> OBJECT_CLASS add(Class<OBJECT_CLASS> obClazz, JSONObject object){
        try {
            return add(obClazz.getConstructor(JSONObject.class).newInstance(object));
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
    public <OBJECT_CLASS extends Model> void addInBackground(final Class<OBJECT_CLASS> obClazz, final JSONObject jsonObject, final ObjectReceiver<OBJECT_CLASS> objectReceiver, final int priority){
        processOnBackground(new DBRequest(priority, "add") {
            @Override
            public void run() {
                final OBJECT_CLASS object = add(obClazz, jsonObject);
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
    public <OBJECT_CLASS extends Model> void addInBackground(final OBJECT_CLASS inObject, final ObjectReceiver<OBJECT_CLASS> objectReceiver, final int priority){
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
    public <OBJECT_CLASS extends Model> void addAll(ArrayList<OBJECT_CLASS> objects){
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
    public <OBJECT_CLASS extends Model> void addAll(Class<OBJECT_CLASS> obClazz, JSONArray array){
        ActiveAndroid.beginTransaction();
        try{
            for(int i = 0; i < array.length();i++){
                OBJECT_CLASS object = obClazz.getConstructor(JSONObject.class).newInstance(array.get(i));
                add(object);
            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            ActiveAndroid.endTransaction();
        }

    }

    public <OBJECT_CLASS extends Model> void addAllInBackground(final Class<OBJECT_CLASS> obClazz, final JSONArray array, final Runnable finishedRunnable, String tag, int priority){
        processOnBackground(new DBRequest(priority, "add "+ tag) {
            @Override
            public void run() {
                addAll(obClazz, array);

                if(finishedRunnable!=null)
                    processOnForeground(finishedRunnable);
            }
        });
    }

    public <OBJECT_CLASS extends Model> void addAllInBackground(final Class<OBJECT_CLASS> obClass, final JSONArray array, final Runnable finishedRunnable, String tag){
        addAllInBackground(obClass, array, finishedRunnable, tag, DBRequest.PRIORITY_LOW);
    }

    /**
     * Retrieves a list of objects from the database without any threading
     * Its recommended not to call this method in the foreground thread
     * @return
     */
    public <OBJECT_CLASS extends Model> List<OBJECT_CLASS> getAll(final Class<OBJECT_CLASS> obClazz){
        return new Select().from(obClazz).execute();
    }

    /**
     * Retrieves a list of objects from the database without any threading with the sort passed
     * Its recommended not to call this method in the foreground thread
     * @param sort - valid SQLLite syntax for sort e.g. name ASC
     * @return
     */
    public <OBJECT_CLASS extends Model> List<OBJECT_CLASS> getAllWithSort(Class<OBJECT_CLASS> obClazz, String sort){
        return new Select().from(obClazz).orderBy(sort).execute();
    }

    /**
     * Fetches objects from this DB on the BG
     * @param receiver - function to call when finished that passes the list of objects that was found
     */
    public <OBJECT_CLASS extends Model> void fetchAll(final Class<OBJECT_CLASS> obClazz, final CollectionReceiver<OBJECT_CLASS> receiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI, "fetch") {
            @Override
            public void run() {
                final List<OBJECT_CLASS> list = getAll(obClazz);
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
    public <OBJECT_CLASS extends Model> void fetchAllWithSort(final Class<OBJECT_CLASS> obClazz, final String sort, final CollectionReceiver<OBJECT_CLASS> receiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI, "fetch") {
            @Override
            public void run() {
                final List<OBJECT_CLASS> list = getAllWithSort(obClazz, sort);
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        receiver.onCollectionReceived(list);
                    }
                });
            }
        });
    };

    public <OBJECT_CLASS extends Model> void fetchAllWithColumnValue(final Class<OBJECT_CLASS> obClazz, final Object value, final String column, final CollectionReceiver<OBJECT_CLASS> receiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI, "fetch") {
            @Override
            public void run() {
                final List<OBJECT_CLASS> list = getAllWithColumnValue(obClazz, column, value);
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
     * @return
     */
    public <OBJECT_CLASS extends Model> OBJECT_CLASS getObjectById(final Class<OBJECT_CLASS> obClazz, Object...ids){
        return new Select().from(obClazz).where(SQLiteUtils.getWhereStatement(obClazz, Cache.getTableInfo(obClazz)), ids).executeSingle();
    }

    /**
     * Returns a single object with the specified column name.
     * Useful for getting objects with a specific primary key
     * @param column
     * @param uid
     * @return
     */
    public <OBJECT_CLASS extends Model> OBJECT_CLASS getObjectByColumnValue(final Class<OBJECT_CLASS> obClazz, String column, Object uid){
        return new Select().from(obClazz).where(column+" =?", uid).executeSingle();
    }

    /**
     * Returns all objects with the specified column name
     * @param column
     * @param value
     * @return
     */
    public <OBJECT_CLASS extends Model> List<OBJECT_CLASS> getAllWithColumnValue(final Class<OBJECT_CLASS> obClazz, String column, Object value){
        return new Select().from(obClazz).where(column + "= ?", value).execute();
    }

    /**
     * Gets all in a table by a group by
     * @param obClazz
     * @param groupBy
     * @param <OBJECT_CLASS>
     * @return
     */
    public <OBJECT_CLASS extends Model> List<OBJECT_CLASS> getAllWithGroupby(final Class<OBJECT_CLASS> obClazz, String groupBy){
        return new Select().from(obClazz).groupBy(groupBy).execute();
    }

    /**
     * Returns the count of rows from this DB manager's DB
     * @return
     */
    public <OBJECT_CLASS extends Model> long getCount(final Class<OBJECT_CLASS> obClazz){
        return DatabaseUtils.queryNumEntries(Cache.openDatabase(), Cache.getTableName(obClazz));
    }

    /**
     * Fetches the count on the DB thread and returns it on the handler
     * @param objectReceiver
     */
    public <OBJECT_CLASS extends Model> void fetchCount(final Class<OBJECT_CLASS> obclazz, final ObjectReceiver<Long> objectReceiver){
        processOnBackground(new DBRequest(DBRequest.PRIORITY_UI) {
            @Override
            public void run() {
                processOnForeground(new Runnable() {
                    @Override
                    public void run() {
                        objectReceiver.onObjectReceived(getCount(obclazz));
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
    public <OBJECT_CLASS extends Model> boolean fetchObject(final Class<OBJECT_CLASS> obClazz, final ObjectRequester<OBJECT_CLASS> requester,  final ObjectReceiver<OBJECT_CLASS> objectReceiver, final Object... uid){
        OBJECT_CLASS object = getObjectById(obClazz, uid);
        if(object==null&&requester!=null){
            processOnForeground(new Runnable() {
                @Override
                public void run() {
                    requester.requestObject(obClazz, objectReceiver, uid);
                }
            });
            return false;
        } else{
            objectReceiver.onObjectReceived(object);
            return true;
        }
    }
    /**
     * Will return the object if its within the DB, if not, it will call upon an object requester to get the data from the API
     *
     * @param objectReceiver
     * @param uid
     * @return true if the object exists in the DB, otherwise its on a BG thread
     */
    public <OBJECT_CLASS extends Model> boolean fetchObject(final Class<OBJECT_CLASS> obClazz, final ObjectReceiver<OBJECT_CLASS> objectReceiver, final Object... uid){
       return fetchObject(obClazz, null, objectReceiver, uid);
    }

}
