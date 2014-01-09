package com.activeandroid.interfaces;

import java.util.List;

/**
 * Created by andrewgrosner
 * Date: 12/8/13
 * Contributors:
 * Description: Used as a callback for items that are a List of Objects returned from the DB
 * This function should be called on the foreground handler thread to perform UI interaction with the data.
 * @param <OBJECT_CLASS>
 */
public interface CollectionReceiver<OBJECT_CLASS>{

    /**
     * Collection was received from the DB
     * @param object
     */
    public void onCollectionReceived(List<OBJECT_CLASS> object);
}

