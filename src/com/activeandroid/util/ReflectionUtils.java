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

import com.activeandroid.DbMetaData;
import com.activeandroid.DefaultMetaData;
import com.activeandroid.Model;
import com.activeandroid.annotation.DatabaseMetaData;
import com.activeandroid.serializer.TypeSerializer;

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

    public static boolean isDbMetaData(Class<?> type) {
        return isSubclassOf(type, DbMetaData.class);
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

    @SuppressWarnings("unchecked")
    public static Class<? extends DbMetaData> getDbMetaDataClass(Class<?> type) {
        // type is DbMetaData, return self
        if (ReflectionUtils.isDbMetaData(type)) {
            return (Class<? extends DbMetaData>) type;

        // is Model, return from annotation, default is DefaultMetaData
        } else if (ReflectionUtils.isModel(type)) {
            final DatabaseMetaData metaDataAnnotation = type.getAnnotation(DatabaseMetaData.class);
            return (metaDataAnnotation != null && metaDataAnnotation.metadataClass() != null)
                    ? metaDataAnnotation.metadataClass()
                    : DefaultMetaData.class;

        // none of above, throw an exception.
        } else {
            throw new IllegalArgumentException("Unable to open database for unknow type "+type.getClass().getSimpleName());
        }
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
}