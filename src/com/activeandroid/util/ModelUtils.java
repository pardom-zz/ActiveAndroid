package com.activeandroid.util;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Table;

import java.lang.reflect.Field;

/**
 * Created by melbic on 25/08/14.
 */
public abstract class ModelUtils {
    /**
     * Check if a field is a Foreignkey
     * @param field
     * @return
     */
    public static boolean isForeignKey(Field field) {
        return Model.class.isAssignableFrom(field.getType());
    }
}
