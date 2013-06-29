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

import static com.activeandroid.Model.Columns.ID;

import com.activeandroid.query.Set;
import com.activeandroid.query.Update;
import com.activeandroid.test.MockModel;

public class UpdateTest extends SqlableTestCase {
	private static final String UPDATE_PREFIX = "UPDATE MockModel ";
	
	public void testUpdate() {
		assertSqlEquals(UPDATE_PREFIX, update());
	}
	
	public void testUpdateSet() {
		assertSqlEquals(UPDATE_PREFIX + "SET " + ID + " = 5 ",
				update().set(ID + " = 5"));
	}
	
	public void testUpdateWhereNoArguments() {
		assertSqlEquals(UPDATE_PREFIX + "SET " + ID + " = 5 WHERE " + ID + " = 1 ",
				update()
					.set(ID + " = 5")
					.where(ID + " = 1"));
	}
	
	public void testUpdateWhereWithArguments() {
		Set set = update()
				.set(ID + " = 5")
				.where(ID + " = ?", 1);
		assertArrayEquals(set.getArguments(), "1");
		assertSqlEquals(UPDATE_PREFIX + "SET " + ID + " = 5 WHERE " + ID + " = ? ",
				set);
		
		set = update()
				.set(ID + " = 5")
				.where(ID + " = ?", 1)
				.where(ID + " IN (?, ?, ?)", 5, 4, 3);
		assertArrayEquals(set.getArguments(), "5", "4", "3");
		assertSqlEquals(UPDATE_PREFIX + "SET " + ID + " = 5 WHERE " + ID + " IN (?, ?, ?) ",
				set);
	}
	
	private Update update() {
		return new Update(MockModel.class);
	}
}
