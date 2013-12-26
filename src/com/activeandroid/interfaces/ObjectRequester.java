package com.activeandroid.interfaces;

import com.activeandroid.Model;

/**
* Created by andrewgrosner
* Date: 12/26/13
* Contributors:
* Description:
*/
public interface ObjectRequester<OBJECT_CLASS extends Model>{
    /**
     * Implement this method to perform a request if the object does not exist in the DB
     * @param objectReceiver
     * @param uid
     */
    public abstract void requestObject(final Class<OBJECT_CLASS> obclazz, final ObjectReceiver<OBJECT_CLASS> objectReceiver, final Object... uid);
}
