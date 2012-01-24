package com.activeandroid.query;

import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.QueryUtils;

public class Join {
	static enum JoinType {
		LEFT, OUTER, INNER, CROSS
	}

	private From mFrom;
	private Class<? extends Model> mType;
	private String mAlias;
	private JoinType mJoinType;
	private String mOn;
	private String[] mUsing;

	Join(From from, Class<? extends Model> table, JoinType joinType) {
		mFrom = from;
		mType = table;
		mJoinType = joinType;
	}

	public Join as(String alias) {
		mAlias = alias;
		return this;
	}

	public From on(String on) {
		mOn = on;
		return mFrom;
	}

	public From on(String on, Object... args) {
		mOn = on;
		mFrom.addArguments(args);
		return mFrom;
	}

	public From using(String... columns) {
		mUsing = columns;
		return mFrom;
	}

	String toSql() {
		StringBuilder sql = new StringBuilder();

		if (mJoinType != null) {
			sql.append(mJoinType.toString() + " ");
		}

		sql.append("JOIN " + QueryUtils.getTableName(mType) + " ");

		if (mAlias != null) {
			sql.append("AS " + mAlias + " ");
		}

		if (mOn != null) {
			sql.append("ON " + mOn + " ");
		}
		else if (mUsing != null) {
			sql.append("USING (" + TextUtils.join(", ", mUsing) + ") ");
		}

		return sql.toString();
	}
}