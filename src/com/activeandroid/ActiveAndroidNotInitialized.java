package com.activeandroid;

public class ActiveAndroidNotInitialized extends RuntimeException {
	public ActiveAndroidNotInitialized() {
		super("ActiveAndroid must be initialized with ActiveAndroid#initialize before interacting with database");
	}
}
