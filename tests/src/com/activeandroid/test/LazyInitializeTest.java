package com.activeandroid.test;

import android.test.AndroidTestCase;
import com.activeandroid.ActiveAndroidNotInitialized;

public class LazyInitializeTest extends AndroidTestCase {
	public void testInitializeDoesNotThrow() throws Exception {
		new MockModel();
	}

	public void testInteractionRequiringDatabaseThrows() {
		boolean expectedExceptionThrown = false;
		try {
			new MockModel().save();
		} catch (ActiveAndroidNotInitialized e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}
}
