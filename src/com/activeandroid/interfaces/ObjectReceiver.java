package com.activeandroid.interfaces;

/**
 * Created by andrewgrosner
 * Date: 12/8/13
 * Contributors:
 * Description: Returns the object when pulled from the database or by request
 * The callback should be run on the main thread to perform UI updates
 */
public interface ObjectReceiver<OBJECT_CLASS> {

    /**
     * Object was received from DB or by API request
     * @param object
     */
    public void onObjectReceived(OBJECT_CLASS object);
}
