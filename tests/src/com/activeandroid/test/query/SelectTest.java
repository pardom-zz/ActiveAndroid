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

import com.activeandroid.query.Select;
import com.activeandroid.test.MockModel;

public class SelectTest extends SqlableTestCase {
	public void testSelectEmpty() {
		assertSqlEquals("SELECT * ", new Select());
	}

	public void testSelectAll() {
		assertSqlEquals("SELECT ALL * ", new Select().all());
		assertSqlEquals("SELECT ALL * ", new Select().distinct().all());
	}

	public void testSelectDistinct() {
		assertSqlEquals("SELECT DISTINCT * ", new Select().distinct());
		assertSqlEquals("SELECT DISTINCT * ", new Select().all().distinct());
	}

	public void testSelectStringColumns() {
		assertSqlEquals("SELECT a, b, c ", new Select("a", "b", "c"));
	}

	public void testSelectDistinctColumns() {
		assertSqlEquals("SELECT DISTINCT a, b, c ",
				new Select("a", "b", "c").distinct());
	}

	public void testFrom() {
		assertSqlEquals("SELECT ALL * FROM MockModel",
				new Select().all().from(MockModel.class));
	}
}
