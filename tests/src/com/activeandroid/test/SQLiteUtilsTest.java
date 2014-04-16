/*
 * Copyright (C) 2014 K.-M. Hansche.
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

package com.activeandroid.test;

import java.util.List;

import com.activeandroid.util.SQLiteUtils;

public class SQLiteUtilsTest extends ActiveAndroidTestCase {

	public void testLexSqlScript() {
		final String s0 = "alter table a add(b int); alter table c add (d int);";
		final String s1 = "alter table a add(b int); alter table c add (d int)";
		final String s2 = "alter table a add(b varchar(255) default ';')";
		final String s3 = "alter table a\nadd(b varchar(255) default ';')\n;";

		List<String> l;
		
		l = SQLiteUtils.lexSqlScript(s0);
		assertArrayEquals(l.toArray(), "alter table a add(b int)", " alter table c add (d int)");

		l = SQLiteUtils.lexSqlScript(s1);
		assertArrayEquals(l.toArray(), "alter table a add(b int)", " alter table c add (d int)");

		l = SQLiteUtils.lexSqlScript(s2);
		assertArrayEquals(l.toArray(), "alter table a add(b varchar(255) default ';')");
		
		l = SQLiteUtils.lexSqlScript(s3);
		assertArrayEquals(l.toArray(), "alter table a\nadd(b varchar(255) default ';')\n");
	}
}
