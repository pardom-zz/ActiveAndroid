package com.activeandroid;

public abstract class TypeParser<T> {
	public abstract Class<?> getType();

	public abstract Object save(T data);

	public abstract T load(Object data);
}
