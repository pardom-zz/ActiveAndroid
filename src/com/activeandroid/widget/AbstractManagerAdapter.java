package com.activeandroid.widget;

import android.widget.BaseAdapter;

import com.activeandroid.manager.DBManager;
import com.activeandroid.Model;
import com.activeandroid.interfaces.CollectionReceiver;

import java.util.List;

/**
 * Created by andrewgrosner
 * Date: 12/14/13
 * Contributors:
 * Description: Provides simple, type-safe implementation in an adapter, fetching objects from the DBManager
 */
public abstract class AbstractManagerAdapter<OBJECT_CLASS extends Model> extends BaseAdapter {

    protected List<OBJECT_CLASS> mObjects;

    private CollectionReceiver<OBJECT_CLASS> mReceiver = new CollectionReceiver<OBJECT_CLASS>() {
        @Override
        public void onCollectionReceived(List<OBJECT_CLASS> object) {
            setData(object);
        }
    };

    public AbstractManagerAdapter(DBManager<OBJECT_CLASS> manager){
        super();
        manager.fetchAll(mReceiver);
    }

    public AbstractManagerAdapter(DBManager<OBJECT_CLASS> manager, String sort){
        super();
        manager.fetchAllWithSort(sort, mReceiver);
    }

    public AbstractManagerAdapter(DBManager<OBJECT_CLASS> manager, Object value, String column){
        super();
        manager.fetchAllWithColumnValue(value, column, mReceiver);
    }

    public void setData(List<OBJECT_CLASS> objects){
        mObjects = objects;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mObjects==null? 0: mObjects.size();
    }

    @Override
    public OBJECT_CLASS getItem(int i) {
        return mObjects==null? null : mObjects.get(i);
    }
}
