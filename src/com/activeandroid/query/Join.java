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

public final class Join implements Sqlable {
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

	@Override
	public String toSql() {
		StringBuilder sqlBuilder = new StringBuilder();
		
		if(mJoinType != null)
			sqlBuilder.append(mJoinType.toString()).append(" ");

		sqlBuilder.append("JOIN ").append(Cache.getTableName(mType)).append(" ");

		if(mAlias != null)
			sqlBuilder.append("AS ").append(mAlias).append(" ");
		
		if(mOn != null)
			sqlBuilder.append("ON ").append(mOn).append(" ");
		else if (mUsing != null)
			sqlBuilder.append("USING (").append(TextUtils.join(", ", mUsing)).append(") ");

		return sqlBuilder.toString();
	}
}