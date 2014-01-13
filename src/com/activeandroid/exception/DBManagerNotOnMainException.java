package com.activeandroid.exception;

/**
 * Created by andrewgrosner
 * Date: 1/10/14
 * Contributors:
 * Description:
 */
public class DBManagerNotOnMainException extends RuntimeException {
    public DBManagerNotOnMainException(String s) {
        super(s);
    }
}
