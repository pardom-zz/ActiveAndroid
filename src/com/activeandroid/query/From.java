package com.activeandroid.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.QueryUtils;
import com.activeandroid.query.Join.JoinType;

public class From {
	private QueryBase mQueryBase;

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

	public From(Class<? extends Model> table, QueryBase queryBase) {
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
		return this;
	}

	public From where(String where, Object... args) {
		mWhere = where;
		addArguments(args);
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

	public From limit(String limit) {
		mLimit = limit;
		return this;
	}

	public From offset(String offset) {
		mOffset = offset;
		return this;
	}

	void addArguments(Object[] args) {
		mArguments.addAll(Arrays.asList(args));
	}

	String toSql() {
		StringBuilder sql = new StringBuilder();

		sql.append(mQueryBase.toSql());

		sql.append("FROM ");

		sql.append(QueryUtils.getTableName(mType) + " ");

		if (mAlias != null) {
			sql.append("AS " + mAlias + " ");
		}

		for (Join join : mJoins) {
			sql.append(join.toSql());
		}

		if (mWhere != null) {
			sql.append("WHERE " + mWhere + " ");
		}

		if (mGroupBy != null) {
			sql.append("GROUP BY " + mGroupBy + " ");
		}

		if (mHaving != null) {
			sql.append("HAVING " + mHaving + " ");
		}

		if (mOrderBy != null) {
			sql.append("ORDER BY " + mOrderBy + " ");
		}

		if (mLimit != null) {
			sql.append("LIMIT " + mLimit + " ");
		}

		if (mOffset != null) {
			sql.append("OFFSET " + mOffset + " ");
		}

		return sql.toString();
	}

	public <T extends Model> ArrayList<T> execute() {
		return Model.rawQuery(mType, toSql(), getArguments());
	}

	public <T extends Model> T executeSingle() {
		return Model.rawQuerySingle(mType, toSql(), getArguments());
	}

	private String[] getArguments() {
		final int size = mArguments.size();
		final String[] args = new String[size];

		for (int i = 0; i < size; i++) {
			args[i] = mArguments.get(i).toString();
		}

		return args;
	}
}