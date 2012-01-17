package com.activeandroid.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	public enum NullConflictAction {
		ROLLBACK, ABORT, FAIL, IGNORE, REPLACE
	}

	public enum ForeignKeyAction {
		SET_NULL, SET_DEFAULT, CASCADE, RESTRICT, NO_ACTION
	}

	public String name();

	public int length() default -1;

	public boolean notNull() default false;

	public NullConflictAction onNullConflict() default NullConflictAction.FAIL;

	public ForeignKeyAction onDelete() default ForeignKeyAction.NO_ACTION;

	public ForeignKeyAction onUpdate() default ForeignKeyAction.NO_ACTION;
}