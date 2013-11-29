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
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Join.JoinType;
import com.activeandroid.util.Log;
import com.activeandroid.util.SQLiteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class From implements Sqlable {
	private Sqlable mQueryBase;

	private Class<? extends Model> mType;
	private String mAlias;
	private List<Join> mJoins;
	private String mWhere;
	private String mGroupBy;
	private String mHaving;
	private String mOrderBy;
	private String mLimit;
	private String mOffset;

	private List<Object> mArguments;

	public From(Class<? extends Model> table, Sqlable queryBase) {
		mType = table;
		mJoins = new ArrayList<Join>();
		mQueryBase = queryBase;

		mJoins = new ArrayList<Join>();
		mArguments = new ArrayList<Object>();
	}

	public From as(String alias) {
		mAlias = alias;
		return this;
	}

	public Join join(Class<? extends Model> table) {
		Join join = new Join(this, table, null);
		mJoins.add(join);
		return join;
	}

	public Join leftJoin(Class<? extends Model> table) {
		Join join = new Join(this, table, JoinType.LEFT);
		mJoins.add(join);
		return join;
	}

	public Join outerJoin(Class<? extends Model> table) {
		Join join = new Join(this, table, JoinType.OUTER);
		mJoins.add(join);
		return join;
	}

	public Join innerJoin(Class<? extends Model> table) {
		Join join = new Join(this, table, JoinType.INNER);
		mJoins.add(join);
		return join;
	}

	public Join crossJoin(Class<? extends Model> table) {
		Join join = new Join(this, table, JoinType.CROSS);
		mJoins.add(join);
		return join;
	}

	public From where(String where) {
		mWhere = where;
		mArguments.clear();

		return this;
	}

	public From where(String where, Object... args) {
		mWhere = where;
		mArguments.clear();
		mArguments.addAll(Arrays.asList(args));

		return this;
	}

	public From groupBy(String groupBy) {
		mGroupBy = groupBy;
		return this;
	}

	public From having(String having) {
		mHaving = having;
		return this;
	}

	public From orderBy(String orderBy) {
		mOrderBy = orderBy;
		return this;
	}

	public From limit(int limit) {
		return limit(String.valueOf(limit));
	}

	public From limit(String limit) {
		mLimit = limit;
		return this;
	}

	public From offset(int offset) {
		return offset(String.valueOf(offset));
	}

	public From offset(String offset) {
		mOffset = offset;
		return this;
	}

	void addArguments(Object[] args) {
		mArguments.addAll(Arrays.asList(args));
	}

	@Override
	public String toSql() {
		StringBuilder sql = new StringBuilder();
		sql.append(mQueryBase.toSql());
		sql.append("FROM ");
		sql.append(Cache.getTableName(mType)).append(" ");

		if (mAlias != null) {
			sql.append("AS ");
			sql.append(mAlias);
			sql.append(" ");
		}

		for (Join join : mJoins) {
			sql.append(join.toSql());
		}

		if (mWhere != null) {
			sql.append("WHERE ");
			sql.append(mWhere);
			sql.append(" ");
		}

		if (mGroupBy != null) {
			sql.append("GROUP BY ");
			sql.append(mGroupBy);
			sql.append(" ");
		}

		if (mHaving != null) {
			sql.append("HAVING ");
			sql.append(mHaving);
			sql.append(" ");
		}

		if (mOrderBy != null) {
			sql.append("ORDER BY ");
			sql.append(mOrderBy);
			sql.append(" ");
		}

		if (mLimit != null) {
			sql.append("LIMIT ");
			sql.append(mLimit);
			sql.append(" ");
		}

		if (mOffset != null) {
			sql.append("OFFSET ");
			sql.append(mOffset);
			sql.append(" ");
		}

		// Don't wast time building the string
		// unless we're going to log it.
		if (Log.isEnabled()) {
			Log.v(sql.toString() + " " + TextUtils.join(",", getArguments()));
		}

		return sql.toString().trim();
	}

	public <T extends Model> List<T> execute() {
		if (mQueryBase instanceof Select) {
			return SQLiteUtils.rawQuery(mType, toSql(), getArguments());
		}
		else {
			SQLiteUtils.execSql(toSql(), getArguments());
			return null;
		}
	}

	public <T extends Model> T executeSingle() {
		if (mQueryBase instanceof Select) {
			limit(1);
			return (T) SQLiteUtils.rawQuerySingle(mType, toSql(), getArguments());
		}
		else {
			SQLiteUtils.execSql(toSql(), getArguments());
			return null;
		}
	}

	public String[] getArguments() {
		final int size = mArguments.size();
		final String[] args = new String[size];

		for (int i = 0; i < size; i++) {
			args[i] = mArguments.get(i).toString();
		}

		return args;
	}
}
