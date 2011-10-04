package com.activeandroid;

public class Application extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ApplicationCache.getInstance().initialize(this);
	}

	@Override
	public void onTerminate() {
		ApplicationCache.getInstance().dispose();
		super.onTerminate();
	}
}