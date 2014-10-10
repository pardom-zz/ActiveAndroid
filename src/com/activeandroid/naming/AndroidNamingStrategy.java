package com.activeandroid.naming;

import java.lang.reflect.Field;

/**
 * This strategy removes the m prefix in Android field names.
 * @author dreipol
 */
public class AndroidNamingStrategy implements ColumnNamingStrategy {
    @Override
    public String translateName(Field f) {
        String columnName = f.getName();
        if (columnName.charAt(0) == 'm' && columnName.length() > 1 && Character.isUpperCase(columnName.codePointAt(1))) {
            columnName = columnName.substring(1);
        }
        return columnName;
    }
}
