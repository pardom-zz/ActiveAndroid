
package com.activeandroid.test.query;

import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.test.MockModel;

import java.util.List;


public class CountTest extends SqlableTestCase {

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
     * Should be a simple count for the entire table.
     */
    public void testCountTableSql() {
        final String expected = "SELECT COUNT(*) FROM MockModel";

        String actual = new Select()
                .from(MockModel.class)
                .toCountSql();

        assertEquals(expected, actual);
    }

    /**
     * Should be a count with the specified where-clause.
     */
    public void testCountWhereClauseSql() {
        final String expected = "SELECT COUNT(*) FROM MockModel WHERE intField = ?";

        String actual = new Select()
                .from(MockModel.class)
                .where("intField = ?", 1)
                .toCountSql();

        assertEquals(expected, actual);
    }

    /**
     * Shouldn't include <i>order by</i> as it has no influence on the result of <i>count</i> and
     * should improve performance.
     */
    public void testCountOrderBySql() {
        final String expected = "SELECT COUNT(*) FROM MockModel WHERE intField <> ? GROUP BY intField";

        String actual = new Select()
                .from(MockModel.class)
                .where("intField <> ?", 0)
                .orderBy("intField")
                .groupBy("intField")
                .toCountSql();

        assertEquals(expected, actual);
    }

    /**
     * Should return the same count as there are entries in the result set/table.
     */
    public void testCountTable() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class);

        final List<MockModel> list = from.execute();
        final int count = from.count();

        assertEquals(3, count);
        assertEquals(list.size(), count);
    }

    /**
     * Should return the same count as there are entries in the result set if the where-clause
     * matches several entries.
     */
    public void testCountWhereClause() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .where("intField = ?", 1);

        final List<MockModel> list = from.execute();
        final int count = from.count();

        assertEquals(2, count);
        assertEquals(list.size(), count);
    }

    /**
     * Should return the same count as there are entries in the result set if the where-clause
     * matches zero entries.
     */
    public void testCountEmptyResult() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .where("intField = ?", 3);

        final List<MockModel> list = from.execute();
        final int count = from.count();

        assertEquals(0, count);
        assertEquals(list.size(), count);
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
        final int count = from.count();

        assertEquals(2, count);
        assertEquals(list.size(), count);
    }

    /**
     * Should return the total number of rows, even if the rows are grouped. May seem weird, just
     * test it in an SQL explorer.
     */
    public void testCountGroupBy() {
        cleanTable();
        populateTable();

        From from = new Select()
                .from(MockModel.class)
                .groupBy("intField")
                .having("intField = 1");

        final List<MockModel> list = from.execute();
        final int count = from.count();

        assertEquals(2, count);
        assertEquals(1, list.size());
    }
}
