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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.ForeignKey;
import com.activeandroid.annotation.PrimaryKey;
import com.activeandroid.annotation.Table;
import com.activeandroid.exception.PrimaryKeyNotFoundException;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

public final class TableInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Class<? extends Model> mType;
	private String mTableName;

	private Map<Field, String> mColumnNames = new HashMap<Field, String>();
    private LinkedList<Field> mPrimaryKeys = new LinkedList<Field>();
    private LinkedList<Field> mForeignKeys = new LinkedList<Field>();

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public TableInfo(Class<? extends Model> type) {
		mType = type;

		final Table tableAnnotation = type.getAnnotation(Table.class);
		if (tableAnnotation != null) {
			mTableName = tableAnnotation.name();
		}
		else {
			mTableName = type.getSimpleName();
		}

		List<Field> fields = new ArrayList<Field>();
        try {
            fields = ReflectionUtils.getAllFields(fields, Class.forName(type.getName()));
            fields.add(getIdField(type));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


		for (Field field : fields) {
			if (field.isAnnotationPresent(Column.class)) {
				final Column columnAnnotation = field.getAnnotation(Column.class);
                String fieldName;
                if(!columnAnnotation.name().equals("")){
                    fieldName = columnAnnotation.name();
                } else{
                    fieldName = field.getName();
                }
				mColumnNames.put(field, fieldName);
			}

            if(field.isAnnotationPresent(PrimaryKey.class) &&
                    field.getAnnotation(PrimaryKey.class).type().equals(PrimaryKey.Type.DEFAULT)){
                mPrimaryKeys.add(field);
            }

            if(field.isAnnotationPresent(ForeignKey.class)){
                mForeignKeys.add(field);
            }
		}

        if(mPrimaryKeys.isEmpty()){
            throw new PrimaryKeyNotFoundException("Table: " + mTableName + " must define a primary key");
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

	public Collection<Field> getFields() {
		return mColumnNames.keySet();
	}

	public String getColumnName(Field field) {
		return mColumnNames.get(field);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Field getIdField(Class<?> type) {
		if (type.equals(Model.class)) {
			try {
				return type.getDeclaredField("mId");
			}
			catch (NoSuchFieldException e) {
				Log.e("Impossible!", e);
			}
		}
		else if (type.getSuperclass() != null) {
			return getIdField(type.getSuperclass());
		}

		return null;
	}

    public LinkedList<Field> getForeignKeys() {
        return mForeignKeys;
    }

    public LinkedList<Field> getPrimaryKeys() {
        return mPrimaryKeys;
    }
}
