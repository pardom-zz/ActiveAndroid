package com.activeandroid.test.Model;

import com.activeandroid.annotation.Column;
import com.activeandroid.test.MockModel;

class ThrowingCallbackMockModel extends MockModel{
	@Column(name="MockColumn")
	public int MockColumn;

	public boolean ThrowExceptions=false;

	public ThrowingCallbackMockModel() {
	}

	protected void onSave() {if (ThrowExceptions) throw new RuntimeException();};
	protected void onDelete() {if (ThrowExceptions) throw new RuntimeException();};
}
