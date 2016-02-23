package com.activeandroid.query;

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

import com.activeandroid.Model;

import java.util.ArrayList;
import java.util.Arrays;

public final class Select implements Sqlable {
    private ArrayList<String> mColumns;
    private boolean mDistinct = false;
    private boolean mAll = false;

    public Select() {
        mColumns = new ArrayList<String>();
    }

    public Select(String... columns) {
        this();
        addColumns(columns);
    }

    public Select(Column... columns) {
        this();
        addColumns(columns);
    }

    public void addColumns(Column... columns) {
        for (Column column : columns) {
            mColumns.add(column.name + " AS " + column.alias);
        }
    }

    public void addColumns(String... columns) {
        mColumns.addAll(ColumnSplitter.split(columns).getColumns());
    }

    public Select all() {
        mDistinct = false;
        mAll = true;

        return this;
    }

    public Select distinct() {
        mDistinct = true;
        mAll = false;

        return this;
    }

    public From from(Class<? extends Model> table) {
        return new From(table, this, SqlMethod.SELECT);
    }

    public boolean hasColumns() {
        return mColumns != null && mColumns.size() > 0;
    }

    @Override
    public String toSql() {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");

        if (mDistinct) {
            sql.append("DISTINCT ");
        } else if (mAll) {
            sql.append("ALL ");
        }

        if (hasColumns()) {
            sql.append(TextUtils.join(", ", mColumns) + " ");
        } else {
            sql.append("* ");
        }

        return sql.toString();
    }

    public static class Column {
        String name;
        String alias;

        public Column(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }
    }

}