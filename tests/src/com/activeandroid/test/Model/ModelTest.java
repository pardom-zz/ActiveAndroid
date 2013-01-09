package com.activeandroid.test.Model;

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
import com.activeandroid.test.ActiveAndroidTestCase;
import com.activeandroid.test.MockModel;

public class ModelTest extends ActiveAndroidTestCase {
	class CallbackMockModel extends MockModel{
		public Boolean s=false;
		public Boolean d=false;

		protected void onSave() {s=true;};
		protected void onDelete() {d=true;};
	}

	public void testGetIdAfterSave(){
		MockModel m=new MockModel();
		m.MockColumn=42;
		m.save();
		assertNotNull("getId() returned null after save()",m.getId());

	}

	public void testGetIdAfterSaveAndSelect(){
		MockModel m=new MockModel();
		m.MockColumn=42;
		m.save();
		MockModel m2=new Select("MockColumn").from(MockModel.class).where("MockColumn=?", 42).executeSingle();
		assertEquals(42, m2.MockColumn); //Check
		assertNotNull("getId() returned null after Select(\"MockColumn\")",m2.getId());
	}

	public void testOnSave(){
		CallbackMockModel m=new CallbackMockModel();
		m.MockColumn=42;
		assertFalse(m.s);
		m.save();
		assertTrue(m.s);
	}

	public void testThrowingOnSave(){
		ThrowingCallbackMockModel m=new ThrowingCallbackMockModel();
		m.ThrowExceptions=true;
		m.MockColumn=42;
		try{m.save();}
		catch (RuntimeException e){}
		assertNull(m.getId());
	}

	public void testOnDelete(){
		CallbackMockModel m=new CallbackMockModel();
		m.MockColumn=42;
		m.save();
		assertFalse(m.d);
		m.delete();
		assertTrue(m.d);
	}

	public void testThrowingOnDelete(){
		ThrowingCallbackMockModel m=new ThrowingCallbackMockModel();
		m.MockColumn=42;
		m.save();
		ThrowingCallbackMockModel m2=new Select("MockColumn").from(ThrowingCallbackMockModel.class).where("MockColumn=?", 42).executeSingle();
		assertNotNull(m2);
		m.ThrowExceptions=true;
		try{m.delete();}
		catch (RuntimeException e){}
		m2=new Select("MockColumn").from(ThrowingCallbackMockModel.class).where("MockColumn=?", 42).executeSingle();
		assertNotNull(m2);
	}
}
