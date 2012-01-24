package com.activeandroid.query;

abstract class QueryBase {
	protected From mFrom;

	abstract String toSql();
}
