package com.activeandroid.naming;

import java.lang.reflect.Field;

/**
 *
 * Provides a way for custom column naming in ActiveAndroid.
 *
 * @author dreipol
 */
public interface ColumnNamingStrategy {

    /**
     * Translates the field name into the column representation.
     *
     * @param f the field object
     * @return the translated column name.
     */
    public String translateName(Field f);
}
