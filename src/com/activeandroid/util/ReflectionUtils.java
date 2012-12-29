package com.activeandroid.util;

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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.app.Application;
import com.activeandroid.migration.Migration;
import com.activeandroid.serializer.TypeSerializer;
import dalvik.system.DexFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class ReflectionUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean isModel(Class<?> type) {
		return isSubclassOf(type, Model.class);
	}

	public static boolean isTypeSerializer(Class<?> type) {
		return isSubclassOf(type, TypeSerializer.class);
	}

	// Meta-data

	@SuppressWarnings("unchecked")
	public static <T> T getMetaData(Context context, String name) {
		try {
			final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);

			if (ai.metaData != null) {
				return (T) ai.metaData.get(name);
			}
		}
		catch (Exception e) {
			Log.w("Couldn't find meta-data: " + name);
		}

		return null;
	}

    public static ArrayList<Class> findClasses(Context application, String packageName) throws IOException {
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
                if (path.contains("bin")) {
                    paths.add(path);
                }
            }
        }

        ArrayList<Class> classes = new ArrayList<Class>();

        for (String path : paths) {
            File file = new File(path);
            classes.addAll(findClassesInPath(file, packageName, application.getClass().getClassLoader()));
        }

        return classes;
    }

    private static List<Class> findClassesInPath(File path, String packageName, ClassLoader classLoader) {
        ArrayList<Class> classes = new ArrayList<Class>();

        if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                classes.addAll(findClassesInPath(file, packageName, classLoader));
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
                    return classes;
                }

                className = className.replace("/", ".");

                int packageNameIndex = className.lastIndexOf(packageName);
                if (packageNameIndex < 0) {
                    return classes;
                }

                className = className.substring(packageNameIndex);
            }

            try {
                Class<?> discoveredClass = Class.forName(className, false, classLoader);
                classes.add(discoveredClass);
            }
            catch (ClassNotFoundException e) {
                Log.e("Couldn't create class.", e);
            }
        }

        return classes;
    }



	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean isSubclassOf(Class<?> type, Class<?> superClass) {
		if (type.getSuperclass() != null) {
			if (type.getSuperclass().equals(superClass)) {
				return true;
			}

			return isSubclassOf(type.getSuperclass(), superClass);
		}

		return false;
	}

    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}