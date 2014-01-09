package com.activeandroid.exception;

/**
 * Created by andrewgrosner
 * Date: 12/12/13
 * Contributors:
 * Description:
 */
public class PrimaryKeyNotFoundException extends RuntimeException {
    public PrimaryKeyNotFoundException(String s) {
        super(s);
    }
}
