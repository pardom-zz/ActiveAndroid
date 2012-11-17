package com.activeandroid;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

class ModelInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Map<Class<? extends Model>, TableInfo> mTableInfos;
	private Map<Class<?>, TypeSerializer> mTypeSerializers;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ModelInfo(Application application) {
		mTableInfos = new HashMap<Class<? extends Model>, TableInfo>();
		mTypeSerializers = new HashMap<Class<?>, TypeSerializer>();

		try {
			scanForModel(application);
		}
		catch (IOException e) {
			Log.e("Couln't open source path.", e);
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

	private void scanForModel(Application application) throws IOException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String packageName = application.getPackageName();
		Enumeration<URL> resources = classLoader.getResources("");

		while (resources.hasMoreElements()) {
			File dir = new File(resources.nextElement().getFile());
			if (dir.getPath().contains("bin")) {
				scanForModelClasses(dir, packageName, application.getClass().getClassLoader());
			}
		}
	}

	private void scanForModelClasses(File dir, String packageName, ClassLoader classLoader) {
		if (!dir.exists()) {
			return;
		}

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				scanForModelClasses(file, packageName, classLoader);
			}
			else if (file.getName().endsWith(".class")) {
				String className = file.getPath().replace("/", ".");
				className = className.substring(className.lastIndexOf("bin.") + 4, className.length() - 6);

				if (className.startsWith("classes.")) {
					className = className.substring(8);
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
						mTypeSerializers.put(typeSerializer.getClass(), typeSerializer);
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
}