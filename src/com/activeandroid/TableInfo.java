package com.activeandroid;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.naming.ColumnNamingStrategy;
import com.activeandroid.naming.FieldNamingStrategy;
import com.activeandroid.util.ReflectionUtils;

public final class TableInfo {
    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    //////////////////////////////////////////////////////////////////////////////////////

    private Class<? extends Model> mType;
    private String mTableName;
    private String mIdName = Table.DEFAULT_ID_NAME;



    private String mUniqueIdentifier = Table.DEFAULT_ID_NAME;
    private Map<Field, String> mColumnNames = new LinkedHashMap<Field, String>();
    private Map<Field, String> mComputedNames = new LinkedHashMap<Field, String>();

    //////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    //////////////////////////////////////////////////////////////////////////////////////

    public TableInfo(Class<? extends Model> type) {
        mType = type;

        final Table tableAnnotation = type.getAnnotation(Table.class);

        ColumnNamingStrategy namingStrategy = new FieldNamingStrategy();
        if (tableAnnotation != null) {
            mTableName = tableAnnotation.name();
            mIdName = tableAnnotation.id();
            mUniqueIdentifier = tableAnnotation.uniqueIdentifier();
            if (mUniqueIdentifier.equals("")) {
                mUniqueIdentifier = getIdField(type).getName();
            }
            try {
                Class<? extends ColumnNamingStrategy> namingClass = tableAnnotation.columnNaming();
                namingStrategy = namingClass.newInstance();
            } catch (InstantiationException e) {
                Log.e("Column naming strategy couldn't be instantiated", e.toString());
            } catch (IllegalAccessException e) {
                Log.e("Column naming strategy couldn't be instantiated", e.toString());
            }
        } else {
            mTableName = type.getSimpleName();
        }

        // Manually add the id column since it is not declared like the other columns.
        Field idField = getIdField(type);
        mColumnNames.put(idField, mIdName);

        List<Field> fields = new LinkedList<Field>(ReflectionUtils.getDeclaredColumnFields(type));
        Collections.reverse(fields);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                final Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.name();
                if (TextUtils.isEmpty(columnName)) {
                    columnName = namingStrategy.translateName(field);
                }

                mColumnNames.put(field, columnName);
            } else if (field.isAnnotationPresent(Computed.class)) {
                final Computed columnAnnotation = field.getAnnotation(Computed.class);
                String name = columnAnnotation.name();
                if (TextUtils.isEmpty(name)) {
                    name = namingStrategy.translateName(field);
                }
                mComputedNames.put(field, name);
            }
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public Class<? extends Model> getType() {
        return mType;
    }

    public String getTableName() {
        return mTableName;
    }

    public String getIdName() {
        return mIdName;
    }

    @Deprecated
    public Collection<Field> getFields() {
        return getColumnFields();
    }

    public Collection<Field> getColumnFields() {
        return mColumnNames.keySet();
    }

    /**
     * @return Fields used as columns (@Column) and fields which can be computed (@Computed)
     */
    public Collection<Field> getAllFields() {
        HashSet<Field> fields = new HashSet<Field>(mColumnNames.keySet());
        fields.addAll(mComputedNames.keySet());
        return fields;
    }

    public String getColumnName(Field field) {
        return mColumnNames.get(field);
    }

    public String getDatabaseName(Field field) {
        String name = mColumnNames.get(field);
        if (name == null) {
            name = mComputedNames.get(field);
        }
        return name;
    }

    private Field getIdField(Class<?> type) {
        if (type.equals(Model.class)) {
            try {
                return type.getDeclaredField("mId");
            } catch (NoSuchFieldException e) {
                Log.e("Impossible!", e.toString());
            }
        } else if (type.getSuperclass() != null) {
            return getIdField(type.getSuperclass());
        }

        return null;
    }

    public String getUniqueIdentifier() {
        return mUniqueIdentifier;
    }
}
