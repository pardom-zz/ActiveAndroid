package com.activeandroid.test;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by kate on 15.08.14.
 * me@rusfearuth.su
 */
@Table(name = "MockChildModel")
public class MockChildModel extends Model
{
	@Column(name = "Parent", onDelete = Column.ForeignKeyAction.CASCADE)
	public MockParentModel parnet;
}
