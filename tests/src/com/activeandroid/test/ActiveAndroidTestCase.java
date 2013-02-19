package com.activeandroid.test;

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

import android.test.ApplicationTestCase;

import com.activeandroid.app.Application;

public abstract class ActiveAndroidTestCase extends ApplicationTestCase<Application> {
	public ActiveAndroidTestCase() {
		super(Application.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static <T> void assertArrayEquals(T[] actual, T... expected) {
		assertEquals(expected.length, actual.length);
		
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}
}
