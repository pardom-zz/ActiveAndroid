package com.activeandroid.test;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * Created by kate on 15.08.14.
 * me@rusfearuth.su
 */
@Table(name = "MockParentModel", id = BaseColumns._ID)
public class MockParentModel extends Model
{
	public List<MockChildModel> getChildren()
	{
		return getMany(MockChildModel.class, "Parent");
	}
}
