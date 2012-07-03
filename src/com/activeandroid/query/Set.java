package com.activeandroid.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.activeandroid.Model;

public class Set implements Sqlable {
	private Update mUpdate;

	private String mSet;
	private String mWhere;

	private List<Object> mSetArguments;
	private List<Object> mWhereArguments;

	public Set(Update queryBase, String set) {
		mUpdate = queryBase;
		mSet = set;

		mSetArguments = new ArrayList<Object>();
		mWhereArguments = new ArrayList<Object>();
	}

	public Set(Update queryBase, String set, Object... args) {
		mUpdate = queryBase;
		mSet = set;

		mSetArguments = new ArrayList<Object>();
		mWhereArguments = new ArrayList<Object>();

		mSetArguments.addAll(Arrays.asList(args));
	}

	public Set where(String where) {
		mWhere = where;
		mWhereArguments.clear();

		return this;
	}

	public Set where(String where, Object... args) {
		mWhere = where;
		mWhereArguments.clear();
		mWhereArguments.addAll(Arrays.asList(args));

		return this;
	}

	@Override
	public String toSql() {
		String sql = "";

		sql += mUpdate.toSql();
		sql += "SET " + mSet + " ";
		sql += "WHERE " + mWhere + " ";

		return sql;
	}

	public void execute() {
		Model.rawQuery(mUpdate.getType(), toSql(), getArguments());
	}

	private String[] getArguments() {
		final int setSize = mSetArguments.size();
		final int whereSize = mWhereArguments.size();
		final String[] args = new String[setSize + whereSize];

		for (int i = 0; i < setSize; i++) {
			args[i] = mSetArguments.get(i).toString();
		}

		for (int i = 0; i < whereSize; i++) {
			args[i] = mWhereArguments.get(i).toString();
		}

		return args;
	}
}