package com.test.christophergastebois.activeandroid;

import android.database.Cursor;

import com.test.christophergastebois.activeandroid.serializer.TypeSerializer;
import com.test.christophergastebois.activeandroid.util.Log;
import com.test.christophergastebois.activeandroid.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Christopher GATEBOIS on 04/07/2014.
 */
public abstract class ViewTable {

    private final ViewTableTableInfo mViewTableTableInfo;

    public ViewTable() {
        mViewTableTableInfo = Cache.getViewTableTableInfo( getClass() );
    }

    // TODO
    public final void loadFromCursor( Cursor cursor ) {
        /**
         * Obtain the columns ordered to fix issue #106 (https://github.com/pardom/ActiveAndroid/issues/106)
         * when the cursor have multiple columns with same name obtained from join tables.
         */
        List<String> columnsOrdered = new ArrayList<String>(Arrays.asList(cursor.getColumnNames()));
        for (Field field : mViewTableTableInfo.getFields()) {
            final String fieldName = mViewTableTableInfo.getColumnName(field);
            Class<?> fieldType = field.getType();
            final int columnIndex = columnsOrdered.indexOf(fieldName);

            if (columnIndex < 0) {
                continue;
            }

            field.setAccessible(true);

            try {
                boolean columnIsNull = cursor.isNull(columnIndex);
                TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
                Object value = null;

                if (typeSerializer != null) {
                    fieldType = typeSerializer.getSerializedType();
                }

                // TODO: Find a smarter way to do this? This if block is necessary because we
                // can't know the type until runtime.
                if (columnIsNull) {
                    field = null;
                }
                else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                    value = cursor.getInt(columnIndex);
                }
                else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                    value = cursor.getInt(columnIndex);
                }
                else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                    value = cursor.getInt(columnIndex);
                }
                else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                    value = cursor.getLong(columnIndex);
                }
                else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                    value = cursor.getFloat(columnIndex);
                }
                else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                    value = cursor.getDouble(columnIndex);
                }
                else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                    value = cursor.getInt(columnIndex) != 0;
                }
                else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
                    value = cursor.getString(columnIndex).charAt(0);
                }
                else if (fieldType.equals(String.class)) {
                    value = cursor.getString(columnIndex);
                }
                else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
                    value = cursor.getBlob(columnIndex);
                }
                else if (ReflectionUtils.isModel(fieldType)) {
                    // no Object in ViewTable, just primitive
                    Log.e(getClass().getName(), "no Object in ViewTable, just primitive");
                }
                else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
                    // no Object in ViewTable, just primitive
                    Log.e(getClass().getName(), "no Object in ViewTable, just primitive");
                }

                // Use a deserializer if one is available
                if (typeSerializer != null && !columnIsNull) {
                    value = typeSerializer.deserialize(value);
                }

                // Set the field value
                if (value != null) {
                    field.set(this, value);
                }
            }
            catch (IllegalArgumentException e) {
                Log.e(e.getClass().getName(), e);
            }
            catch (IllegalAccessException e) {
                Log.e(e.getClass().getName(), e);
            }
            catch (SecurityException e) {
                Log.e(e.getClass().getName(), e);
            }
        }

        //if (mId != null) {
        //    Cache.addEntity(this);
        //}
    }

}
