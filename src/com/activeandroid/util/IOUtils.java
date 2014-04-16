
package com.activeandroid.util;

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;

import com.activeandroid.util.Log;


public class IOUtils {

    /**
     * Unconditionally close a {@link Closeable}.
     * <p/>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored. This is
     * typically used in finally blocks.
     * @param closeable A {@link Closeable} to close.
     */
    public static void closeQuietly(final Closeable closeable) {

        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (final IOException e) {
            Log.e("Couldn't close closeable.", e);
        }
    }

    /**
     * Unconditionally close a {@link Cursor}.
     * <p/>
     * Equivalent to {@link Cursor#close()}, except any exceptions will be ignored. This is
     * typically used in finally blocks.
     * @param cursor A {@link Cursor} to close.
     */
    public static void closeQuietly(final Cursor cursor) {

        if (cursor == null) {
            return;
        }

        try {
            cursor.close();
        } catch (final Exception e) {
            Log.e("Couldn't close cursor.", e);
        }
    }
}
