
package com.activeandroid.test.parser;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.Configuration;
import com.activeandroid.DatabaseHelper;
import com.activeandroid.test.ActiveAndroidTestCase;


public class ParserConfigurationTest extends ActiveAndroidTestCase {

    /**
     * Should try to use the legacy parser by default, which is be unable to handle the SQL script.
     */
    public void testLegacyMigration() {

        try {
            Configuration configuration = new Configuration.Builder(getContext())
                    .setDatabaseName("migration.db")
                    .setDatabaseVersion(2)
                    .create();

            DatabaseHelper helper = new DatabaseHelper(configuration);
            SQLiteDatabase db = helper.getWritableDatabase();
            helper.onUpgrade(db, 1, 2);

            fail("Should not be able to parse the SQL script.");

        } catch (SQLException e) {
            final String message = e.getMessage();

            assertNotNull(message);
            assertTrue(message.contains("syntax error"));
            assertTrue(message.contains("near \"MockMigration\""));
        }
    }

    /**
     * Should use the new parser if configured to do so.
     */
    public void testDelimitedMigration() {
        Configuration configuration = new Configuration.Builder(getContext())
                .setSqlParser(Configuration.SQL_PARSER_DELIMITED)
                .setDatabaseName("migration.db")
                .setDatabaseVersion(2)
                .create();

        DatabaseHelper helper = new DatabaseHelper(configuration);
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.onUpgrade(db, 1, 2);
    }
}
