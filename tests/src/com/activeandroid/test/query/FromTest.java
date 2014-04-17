package com.activeandroid.test.query;

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

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.test.MockModel;

public class FromTest extends SqlableTestCase {
	private static final String SELECT_PREFIX = "SELECT ALL * FROM MockModel ";
	
	public void testLimit() {
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10",
				from().limit(10));
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10",
				from().limit("10"));
	}
	
	public void testOffset() {
		assertSqlEquals(SELECT_PREFIX + "OFFSET 10",
				from().offset(10));
		assertSqlEquals(SELECT_PREFIX + "OFFSET 10",
				from().offset("10"));
	}
	
	public void testLimitOffset() {
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10 OFFSET 20",
				from().offset(20).limit(10));
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10 OFFSET 20",
				from().limit(10).offset(20));
	}
	
	public void testAs() {
		assertSqlEquals(SELECT_PREFIX + "AS a",
				from().as("a"));
	}
	
	public void testOrderBy() {
		assertSqlEquals(SELECT_PREFIX + "ORDER BY Id DESC",
				from().orderBy("Id DESC"));
	}
	
	public void testWhereNoArguments() {
		assertSqlEquals(SELECT_PREFIX + "WHERE Id = 5",
				from().where("Id = 5"));
		
		assertSqlEquals(SELECT_PREFIX + "WHERE Id = 1 AND Id = 2 AND Id = 5",
				from().where("Id = 1").where("Id = 2").where("Id = 5"));
	}
	
	public void testWhereWithArguments() {
		From query = from().where("Id = ?", 5);
		assertArrayEquals(query.getArguments(), "5");
		assertSqlEquals(SELECT_PREFIX + "WHERE Id = ?",
				query);
		
		query = from().where("Id > ? AND Id < ?", 5, 10);
		assertArrayEquals(query.getArguments(), "5", "10");
		assertSqlEquals(SELECT_PREFIX + "WHERE Id > ? AND Id < ?",
				query);
		
        // Chained
		query = from()
				.where("Id != ?", 10)
				.where("Id IN (?, ?, ?)", 5, 10, 15)
				.where("Id > ? AND Id < ?", 5, 10);
		assertArrayEquals(query.getArguments(), "10", "5", "10", "15", "5", "10");
		assertSqlEquals(SELECT_PREFIX + "WHERE Id != ? AND Id IN (?, ?, ?) AND Id > ? AND Id < ?",
				query);
	}

	public void testWhereChaining() {
	    
	    From expected = from()
	            .where("a = ? AND b = ?", 1, 2);
	    
	    From actual = from()
	            .where("a = ?", 1, 2)
	            .where("b = ?", 1, 2);
	    
	    assertSqlEquals(expected, actual);
	}
	
   public void testWhereAndChaining() {

       From expected = from()
               .where("a = ? AND b = ?", 1, 2);

       From actual = from()
               .where("a = ?", 1)
               .and("b = ?", 2);

       assertSqlEquals(expected, actual);
   }

   public void testWhereOrChaining() {

       From expected = from()
               .where("a = ? OR b = ?", 1, 2);

       From actual = from()
               .where("a = ?", 1)
               .or("b = ?", 2);

       assertSqlEquals(expected, actual);
   }

   public void testWhereAndOrChaining() {

       From expected = from()
               .where("a = ? OR (b = ? AND c = ?)", 1, 2, 3);

       From actual = from()
               .where("a = ?", 1)
               .or("(b = ? AND c = ?)", 2, 3);

       assertSqlEquals(expected, actual);
   }

   public void testWhereAlternateAndOrChaining() {

       From expected = from()
               .where("a = ? OR (b = ? AND c = ?)", 1, 2, 3);

       From actual = from()
               .where("a = ?", 1)
               .or("(b = ?", 2)
               .and("c = ?)", 3);

       assertSqlEquals(expected, actual);
   }

    // Test with 'no arguments' and 'with arguments' chained together.
    public void testWhereWithNoArgumentsAndWithArguments() {
        From query = from().where("Id = 5");
        query.where("Id > ?", 4);
        assertArrayEquals(query.getArguments(), "4");
        assertSqlEquals(SELECT_PREFIX + "WHERE Id = 5 AND Id > ?",
                query);
    }
	
	public void testSingleJoin() {
		assertSqlEquals(SELECT_PREFIX + "JOIN JoinModel ON MockModel.Id = JoinModel.Id",
				from().join(JoinModel.class).on("MockModel.Id = JoinModel.Id"));
		
		assertSqlEquals(SELECT_PREFIX + "AS a JOIN JoinModel AS b ON a.Id = b.Id",
				from().as("a").join(JoinModel.class).as("b").on("a.Id = b.Id"));
		
		assertSqlEquals(SELECT_PREFIX + "JOIN JoinModel USING (Id, other)",
				from().join(JoinModel.class).using("Id", "other"));
	}
	
	public void testJoins() {
		assertSqlEquals(SELECT_PREFIX + "JOIN JoinModel ON Id JOIN JoinModel2 ON Id",
				from().join(JoinModel.class).on("Id")
				.join(JoinModel2.class).on("Id"));
	}
	
	public void testJoinTypes() {
		assertSqlEquals(SELECT_PREFIX + "INNER JOIN JoinModel ON",
				from().innerJoin(JoinModel.class).on(""));
		assertSqlEquals(SELECT_PREFIX + "OUTER JOIN JoinModel ON",
				from().outerJoin(JoinModel.class).on(""));
		assertSqlEquals(SELECT_PREFIX + "CROSS JOIN JoinModel ON",
				from().crossJoin(JoinModel.class).on(""));
	}
	
	public void testGroupByHaving() {
		assertSqlEquals(SELECT_PREFIX + "GROUP BY Id",
				from().groupBy("Id"));
		assertSqlEquals(SELECT_PREFIX + "GROUP BY Id HAVING Id = 1",
				from().groupBy("Id").having("Id = 1"));
		assertSqlEquals(SELECT_PREFIX + "GROUP BY Id HAVING Id = 1",
				from().having("Id = 1").groupBy("Id"));
	}
	
	public void testAll() {
		final String expectedSql = SELECT_PREFIX + "AS a JOIN JoinModel USING (Id) WHERE Id > 5 GROUP BY Id HAVING Id < 10 LIMIT 5 OFFSET 10";
		
		// Try a few different orderings, shouldn't change the output
		assertSqlEquals(expectedSql,
				from()
					.as("a")
					.where("Id > 5")
					.join(JoinModel.class).using("Id")
					.groupBy("Id")
					.having("Id < 10")
					.limit(5)
					.offset(10));
		assertSqlEquals(expectedSql,
				from()
					.offset(10)
					.having("Id < 10")
					.join(JoinModel.class).using("Id")
					.limit(5)
					.as("a")
					.where("Id > 5")
					.groupBy("Id"));
		assertSqlEquals(expectedSql,
				from()
					.join(JoinModel.class).using("Id")
					.offset(10)
					.having("Id < 10")
					.where("Id > 5")
					.groupBy("Id")
					.limit(5)
					.as("a"));
	}
	
	private From from() {
		return new Select().all().from(MockModel.class);
	}
	
	@Table(name = "JoinModel")
	private static class JoinModel extends Model {
	}
	
	@Table(name = "JoinModel2")
	private static class JoinModel2 extends Model {
	}
}
