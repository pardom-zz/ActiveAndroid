package com.activeandroid;

/**
 * Created by melbic on 20/10/14.
 */
public class ModelSaveResult<T extends Model> {
    private T mEntity;
    private boolean mIsNew;

    public ModelSaveResult(T entity, boolean isNew) {

        mEntity = entity;
        mIsNew = isNew;
    }

    public T getEntity() {
        return mEntity;
    }

    public boolean isNew() {
        return mIsNew;
    }


}
