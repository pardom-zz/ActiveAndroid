package com.activeandroid;

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

import android.content.Context;

import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

final class ViewTableInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Map<Class<? extends ViewTable>, ViewTableTableInfo> mTableInfos  = new HashMap<Class<? extends ViewTable>, ViewTableTableInfo>();

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ViewTableInfo(Configuration configuration) {
		if (!loadViewTableFromMetaData(configuration)) {
			try {
				scanForViewTable(configuration.getContext());
			}
			catch (IOException e) {
				Log.e("Couldn't open source path.", e);
			}
		}

		Log.i("ModelInfo loaded.");
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public Collection<ViewTableTableInfo> getViewTableTableInfos() {
		return mTableInfos.values();
	}

	public ViewTableTableInfo getViewTableTableInfo(Class<? extends ViewTable> type) {
		return mTableInfos.get(type);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private boolean loadViewTableFromMetaData(Configuration configuration) {
		if (!configuration.viewTableConfigurationIsValid()) {
			return false;
		}

		final List<Class<? extends ViewTable>> viewTables = configuration.getViewTableClasses();
		if (viewTables != null) {
			for (Class<? extends ViewTable> viewTable : viewTables) {
				mTableInfos.put(viewTable, new ViewTableTableInfo(viewTable));
			}
		}

		return true;
	}

	private void scanForViewTable(Context context) throws IOException {
		String packageName = context.getPackageName();
		String sourcePath = context.getApplicationInfo().sourceDir;
		List<String> paths = new ArrayList<String>();

		if (sourcePath != null && !(new File(sourcePath).isDirectory())) {
			DexFile dexfile = new DexFile(sourcePath);
			Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				paths.add(entries.nextElement());
			}
		}
		// Robolectric fallback
		else {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> resources = classLoader.getResources("");

			while (resources.hasMoreElements()) {
				String path = resources.nextElement().getFile();
				if (path.contains("bin") || path.contains("classes")) {
					paths.add(path);
				}
			}
		}

		for (String path : paths) {
			File file = new File(path);
			scanForViewTableClasses(file, packageName, context.getClassLoader());
		}
	}

	private void scanForViewTableClasses(File path, String packageName, ClassLoader classLoader) {
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				scanForViewTableClasses(file, packageName, classLoader);
			}
		}
		else {
			String className = path.getName();

			// Robolectric fallback
			if (!path.getPath().equals(className)) {
				className = path.getPath();

				if (className.endsWith(".class")) {
					className = className.substring(0, className.length() - 6);
				}
				else {
					return;
				}

				className = className.replace(System.getProperty("file.separator"), ".");

				int packageNameIndex = className.lastIndexOf(packageName);
				if (packageNameIndex < 0) {
					return;
				}

				className = className.substring(packageNameIndex);
			}

			try {
				Class<?> discoveredClass = Class.forName(className, false, classLoader);
				if (ReflectionUtils.isViewTable(discoveredClass)) {

					Class<? extends ViewTable> viewTableClass = (Class<? extends ViewTable>) discoveredClass;
					mTableInfos.put(viewTableClass, new ViewTableTableInfo(viewTableClass));
				}
			}
			catch (ClassNotFoundException e) {
				Log.e("Couldn't create class.", e);
			}
		}
	}
}
