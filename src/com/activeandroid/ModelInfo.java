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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.activeandroid.serializer.CalendarSerializer;
import com.activeandroid.serializer.SqlDateSerializer;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.serializer.UtilDateSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;
import dalvik.system.DexFile;

final class ModelInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private final static String AA_MODELS = "AA_MODELS";
	private final static String AA_SERIALIZERS = "AA_SERIALIZERS";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Map<Class<? extends Model>, TableInfo> mTableInfos = new HashMap<Class<? extends Model>, TableInfo>();
	private Map<Class<?>, TypeSerializer> mTypeSerializers = new HashMap<Class<?>, TypeSerializer>() {
		{
			put(Calendar.class, new CalendarSerializer());
			put(java.sql.Date.class, new SqlDateSerializer());
			put(java.util.Date.class, new UtilDateSerializer());
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ModelInfo(Application application) {
		if (!loadModelFromMetaData(application)) {
			try {
				scanForModel(application);
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

	public Collection<TableInfo> getTableInfos() {
		return mTableInfos.values();
	}

	public TableInfo getTableInfo(Class<? extends Model> type) {
		return mTableInfos.get(type);
	}

	@SuppressWarnings("unchecked")
	public List<Class<? extends Model>> getModelClasses() {
		return (List<Class<? extends Model>>) mTableInfos.keySet();
	}

	public TypeSerializer getTypeSerializer(Class<?> type) {
		return mTypeSerializers.get(type);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private boolean loadModelFromMetaData(Application application) {
		final String modelList = ReflectionUtils.getMetaData(application, AA_MODELS);
		final String serializerList = ReflectionUtils.getMetaData(application, AA_SERIALIZERS);

		if (!TextUtils.isEmpty(modelList)) {
			loadModelList(application, modelList.split(","));
		}

		if (!TextUtils.isEmpty(serializerList)) {
			loadSerializerList(application, serializerList.split(","));
		}

		return mTableInfos.size() > 0;
	}

	private void loadModelList(Context context, String[] models) {
		final ClassLoader classLoader = context.getClass().getClassLoader();
		for (String model : models) {
			model = ensureFullClassName(context, model);

			try {
				Class modelClass = Class.forName(model, false, classLoader);
				mTableInfos.put(modelClass, new TableInfo(modelClass));
			}
			catch (ClassNotFoundException e) {
				Log.e("Couldn't create class.", e);
			}
		}
	}

	private void loadSerializerList(Context context, String[] serializers) {
		final ClassLoader classLoader = context.getClass().getClassLoader();
		for (String serializer : serializers) {
			serializer = ensureFullClassName(context, serializer);

			try {
				Class serializerClass = Class.forName(serializer, false, classLoader);
				TypeSerializer typeSerializer = (TypeSerializer) serializerClass.newInstance();
				mTypeSerializers.put(typeSerializer.getDeserializedType(), typeSerializer);
			}
			catch (ClassNotFoundException e) {
				Log.e("Couldn't create class.", e);
			}
			catch (InstantiationException e) {
				Log.e("Couldn't instantiate TypeSerializer.", e);
			}
			catch (IllegalAccessException e) {
				Log.e("IllegalAccessException", e);
			}
		}
	}

	private String ensureFullClassName(Context context, String name) {
		String packageName = context.getPackageName();
		if (name.startsWith(packageName)) {
			return name.trim();
		}

		return packageName + name.trim();
	}

	private void scanForModel(Application application) throws IOException {
		String packageName = application.getPackageName();
		String sourcePath = application.getApplicationInfo().sourceDir;
		List<String> paths = new ArrayList<String>();

		if (sourcePath != null) {
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
			scanForModelClasses(file, packageName, application.getClass().getClassLoader());
		}
	}

	private void scanForModelClasses(File path, String packageName, ClassLoader classLoader) {
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				scanForModelClasses(file, packageName, classLoader);
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

				className = className.replace("/", ".");

				int packageNameIndex = className.lastIndexOf(packageName);
				if (packageNameIndex < 0) {
					return;
				}

				className = className.substring(packageNameIndex);
			}

			try {
				Class<?> discoveredClass = Class.forName(className, false, classLoader);
				if (ReflectionUtils.isModel(discoveredClass)) {
					@SuppressWarnings("unchecked")
					Class<? extends Model> modelClass = (Class<? extends Model>) discoveredClass;
					mTableInfos.put(modelClass, new TableInfo(modelClass));
				}
				else if (ReflectionUtils.isTypeSerializer(discoveredClass)) {
					TypeSerializer typeSerializer = (TypeSerializer) discoveredClass.newInstance();
					mTypeSerializers.put(typeSerializer.getDeserializedType(), typeSerializer);
				}
			}
			catch (ClassNotFoundException e) {
				Log.e("Couldn't create class.", e);
			}
			catch (InstantiationException e) {
				Log.e("Couldn't instantiate TypeSerializer.", e);
			}
			catch (IllegalAccessException e) {
				Log.e("IllegalAccessException", e);
			}
		}
	}
}
