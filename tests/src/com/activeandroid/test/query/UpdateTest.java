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

import com.activeandroid.query.Set;
import com.activeandroid.query.Update;
import com.activeandroid.test.MockModel;

public class UpdateTest extends SqlableTestCase {
	private static final String UPDATE_PREFIX = "UPDATE MockModel ";
	
	public void testUpdate() {
		assertSqlEquals(UPDATE_PREFIX, update());
	}
	
	public void testUpdateSet() {
		assertSqlEquals(UPDATE_PREFIX + "SET Id = 5 ",
				update().set("Id = 5"));
	}
	
	public void testUpdateWhereNoArguments() {
		assertSqlEquals(UPDATE_PREFIX + "SET Id = 5 WHERE Id = 1 ",
				update()
					.set("Id = 5")
					.where("Id = 1"));
	}
	
	public void testUpdateWhereWithArguments() {
		Set set = update()
				.set("Id = 5")
				.where("Id = ?", 1);
		assertArrayEquals(set.getArguments(), "1");
		assertSqlEquals(UPDATE_PREFIX + "SET Id = 5 WHERE Id = ? ",
				set);
		
		set = update()
				.set("Id = 5")
				.where("Id = ?", 1)
				.where("Id IN (?, ?, ?)", 5, 4, 3);
		assertArrayEquals(set.getArguments(), "5", "4", "3");
		assertSqlEquals(UPDATE_PREFIX + "SET Id = 5 WHERE Id IN (?, ?, ?) ",
				set);
	}
	
	private Update update() {
		return new Update(MockModel.class);
	}
}
