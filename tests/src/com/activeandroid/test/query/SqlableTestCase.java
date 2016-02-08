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

import com.activeandroid.query.Sqlable;
import com.activeandroid.test.ActiveAndroidTestCase;

public abstract class SqlableTestCase extends ActiveAndroidTestCase {
	public static void assertSqlEquals(String expected, Sqlable actual) {
		assertEquals(expected.trim(), actual.toSql().trim());
	}
	
	public static void assertSqlEquals(Sqlable expected, Sqlable actual) {
	    assertSqlEquals(expected.toSql(), actual);
	}
}
