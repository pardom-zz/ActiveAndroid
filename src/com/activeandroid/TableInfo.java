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

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.util.ReflectionUtils;
import com.activeandroid.util.SQLiteUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class TableInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Class<? extends Model> mType;
	private String mTableName;
	private String mIdName = Table.DEFAULT_ID_NAME;
    private String mCustomIdName = Table.DEFAULT_CUSTOM_ID_NAME;


    private Map<Field, String> mColumnNames = new LinkedHashMap<Field, String>();

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public TableInfo(Class<? extends Model> type) {
		mType = type;

		final Table tableAnnotation = type.getAnnotation(Table.class);

        if (tableAnnotation != null) {
			mTableName = tableAnnotation.name();
			mIdName = tableAnnotation.id();
            mCustomIdName = tableAnnotation.customIdName();
		}
		else {
			mTableName = type.getSimpleName();
        }

        // Manually add the id column since it is not declared like the other columns.
        Field idField = getIdField(type);
        mColumnNames.put(idField, mIdName);

        List<Field> fields = new LinkedList<Field>(ReflectionUtils.getDeclaredColumnFields(type));
        Collections.reverse(fields);

        boolean isCustomIdSupplied = !mCustomIdName.equals(Table.DEFAULT_CUSTOM_ID_NAME);
        Field customIdField = null;

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                final Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.name();
                if (TextUtils.isEmpty(columnName)) {
                    columnName = field.getName();
                }

                if (isCustomIdSupplied && columnName.equals(mCustomIdName)) {
                    customIdField = field;
                }

                mColumnNames.put(field, columnName);
            }
        }

        if (isCustomIdSupplied) {
            if (customIdField == null) {
                Log.e(com.activeandroid.util.Log.sTag,
                        "Given custom Id doesn't exists in table columns",
                        new Throwable("Custom Id defined as " + mCustomIdName + ", but doesn't exists in table columns."));
            }
            else if (!SQLiteUtils.TYPE_MAP.containsKey(customIdField.getType())) {
                Log.e(com.activeandroid.util.Log.sTag,
                        "Given custom Id is of an illegal type",
                        new Throwable("Custom Id type " + customIdField.getType() + " isn't a legal id type"));
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

    public String getCustomIdName() {
        return mCustomIdName;
    }

    public Collection<Field> getFields() {
		return mColumnNames.keySet();
	}

	public String getColumnName(Field field) {
		return mColumnNames.get(field);
	}

    private Field getIdField(Class<?> type) {
        if (type.equals(Model.class)) {
            try {
                return type.getDeclaredField("mId");
            }
            catch (NoSuchFieldException e) {
                Log.e("Impossible!", e.toString());
            }
        }
        else if (type.getSuperclass() != null) {
            return getIdField(type.getSuperclass());
        }

        return null;
    }

}
