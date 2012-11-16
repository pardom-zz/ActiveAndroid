package com.activeandroid;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

import dalvik.system.DexFile;

class ModelInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Map<Class<? extends Model>, TableInfo> mTableInfos = new HashMap<Class<? extends Model>, TableInfo>();
	private Map<Class<?>, TypeSerializer> mTypeSerializers = new HashMap<Class<?>, TypeSerializer>();

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public ModelInfo(Context context) {
		try {
			final String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
			final DexFile dexfile = new DexFile(path);
			final Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				final String name = entries.nextElement();

				if (!name.contains("com.activeandroid.serializer") && name.contains("com.activeandroid")) {
					continue;
				}

				try {
					Class<?> discoveredClass = Class.forName(name, false, context.getClass().getClassLoader());
					if (ReflectionUtils.isModel(discoveredClass)) {
						Class<? extends Model> modelClass = (Class<? extends Model>) discoveredClass;
						mTableInfos.put(modelClass, new TableInfo(modelClass));
					}
					else if (ReflectionUtils.isTypeSerializer(discoveredClass)) {
						TypeSerializer typeSerializer = (TypeSerializer) discoveredClass.newInstance();
						mTypeSerializers.put(typeSerializer.getClass(), typeSerializer);
					}
				}
				catch (ClassNotFoundException e) {
					Log.e("Couldn't create class.", e.getMessage());
				}
				catch (InstantiationException e) {
					Log.e("Couldn't instantiate TypeSerializer.", e.getMessage());
				}
				catch (IllegalAccessException e) {
					Log.e("IllegalAccessException: ", e.getMessage());
				}
			}
		}
		catch (IOException e) {
			Log.e("Couln't open source path.", e.getMessage());
		}
		catch (NameNotFoundException e) {
			Log.e("Couldn't find ApplicationInfo for package name.", e.getMessage());
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public List<TableInfo> getTableInfos() {
		return (List<TableInfo>) mTableInfos.values();
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
}