
package com.activeandroid.test.query;

import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.test.MockModel;

import java.util.List;


public class ExistsTest extends SqlableTestCase {

    private void cleanTable() {
        new Delete().from(MockModel.class).execute();
    }

    private void populateTable() {
        MockModel m1 = new MockModel();
        MockModel m2 = new MockModel();
        MockModel m3 = new MockModel();

        m1.intField = 1;
        m2.intField = 1;
        m3.intField = 2;

        m1.save();
        m2.save();
        m3.save();
    }

    /**
     * Should return {@code true} since the result set/table isn't empty.
     */
    public void testExistsTable() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class);

        final List<MockModel> list = from.execute();
        final boolean exists = from.exists();

        assertTrue(exists);
        assertTrue(list.size() > 0);
    }

    /**
     * Should be a simple exists for the entire table.
     */
    public void testCountTableSql() {
        final String expected = "SELECT EXISTS(SELECT 1 FROM MockModel )";

        String actual = new Select()
                .from(MockModel.class)
                .toExistsSql();

        assertEquals(expected, actual);
    }

    /**
     * Should be an exists with the specified where-clause.
     */
    public void testCountWhereClauseSql() {
        final String expected = "SELECT EXISTS(SELECT 1 FROM MockModel WHERE intField = ? )";

        String actual = new Select()
                .from(MockModel.class)
                .where("intField = ?", 1)
                .toExistsSql();

        assertEquals(expected, actual);
    }

    /**
     * Shouldn't include <i>order by</i> as it has no influence on the result of <i>exists</i> and
     * should improve performance.
     */
    public void testCountOrderBySql() {
        final String expected = "SELECT EXISTS(SELECT 1 FROM MockModel WHERE intField <> ? GROUP BY intField )";

        String actual = new Select()
                .from(MockModel.class)
                .groupBy("intField")
                .orderBy("intField")
                .where("intField <> ?", 0)
                .toExistsSql();

        assertEquals(expected, actual);
    }

    /**
     * Should return {@code true} since the where-clause matches rows and thus the result set isn't
     * empty.
     */
    public void testExistsWhereClause() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .where("intField = ?", 1);

        final List<MockModel> list = from.execute();
        final boolean exists = from.exists();

        assertTrue(exists);
        assertTrue(list.size() > 0);
    }

    /**
     * Should return {@code false} since the where-clause matches zero rows and thus the result set
     * is empty.
     */
    public void testExistsEmptyResult() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .where("intField = ?", 3);

        final List<MockModel> list = from.execute();
        final boolean exists = from.exists();

        assertFalse(exists);
        assertFalse(list.size() > 0);
    }

    /**
     * Should not change the result if order by is used.
     */
    public void testCountOrderBy() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .where("intField = ?", 1)
                .orderBy("intField ASC");

        final List<MockModel> list = from.execute();
        final boolean exists = from.exists();

        assertTrue(exists);
        assertTrue(list.size() > 0);
    }

    /**
     * Should not change the result if group by is used.
     */
    public void testCountGroupBy() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .groupBy("intField")
                .having("intField = 1");

        final List<MockModel> list = from.execute();
        final boolean exists = from.exists();

        assertTrue(exists);
        assertTrue(list.size() > 0);
    }

    /**
     * Should not exist if group by eliminates all rows.
     */
    public void testCountGroupByEmpty() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .groupBy("intField")
                .having("intField = 3");

        final List<MockModel> list = from.execute();
        final boolean exists = from.exists();

        assertFalse(exists);
        assertFalse(list.size() > 0);
    }
}
