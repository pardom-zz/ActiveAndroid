package com.activeandroid.query;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by samuelbichsel on 15/02/16.
 */
public class ColumnSplitter {
    private final String mTable;
    private final String[] mColumns;
    private final ArrayList<String> mProcessedColumns;

    private ColumnSplitter(final String table, String... columns) {

        mTable = table;
        mColumns = columns;
        mProcessedColumns = new ArrayList<String>();
    }

    public static ColumnSplitter split(String table, String... columns) {
        ColumnSplitter columnSplitter = new ColumnSplitter(table, columns);
        columnSplitter.split();
        return columnSplitter;
    }

    public static ColumnSplitter split(String... columns) {
        return split(null, columns);
    }

    public ArrayList<String> getColumns() {
        return mProcessedColumns;
    }

    public String getString() {
        String join = TextUtils.join(", ", mProcessedColumns);
        if (join.isEmpty()) {
            if (mTable != null) {
                join = mTable + ".";
            }
            join += "*";
        }
        return join;
    }

    private void split() {
        for (String col : mColumns) {
            String[] strings = TextUtils.split(col, Pattern.compile(",\\s*"));
            List<String> columnsList = Arrays.asList(strings);
            if (mTable != null) {
                for (String input : columnsList) {
                    mProcessedColumns.add(mTable + "." + input);
                }
            } else {
                mProcessedColumns.addAll(columnsList);
            }
        }

    }
}
