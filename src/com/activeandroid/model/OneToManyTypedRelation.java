package com.activeandroid.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.util.Log;

public abstract class OneToManyTypedRelation<T1 extends Model> extends Model {

	@Column (name = "entity1")
	private T1 entity1;
	@Column (name = "entity2Type")
	private String entity2Type;
	@Column (name = "entity2")
	private Model entity2;

	public static <T1 extends Model> void setRelations(Class<? extends OneToManyTypedRelation<T1>> relation, T1 entity1, List<Model> entities2) {
		if (entity1.getId() == null)
			throw new IllegalArgumentException(entity1.getClass().getSimpleName() + " is not saved to database yet, aborting");
		for (Model entity2 : entities2) {
			if (entity2.getId() == null)
				throw new IllegalArgumentException(entity2.getClass().getSimpleName() + " is not saved to database yet, aborting");
		}

		new Delete().from(relation).where("entity1 = ?", entity1.getId()).execute();
		try {
			ArrayList<OneToManyTypedRelation<T1>> connections = new ArrayList<OneToManyTypedRelation<T1>>();
			for (Model entity2 : entities2) {
				OneToManyTypedRelation<T1> connection = relation.newInstance();
				connection.entity1 = entity1;
				connection.entity2Type = entity2.getClass().getCanonicalName();
				connection.entity2 = entity2;
				connections.add(connection);
			}
			saveMultiple(connections);
		} catch (Exception e) {
			Log.e("Cannot create instance of class " + relation.getSimpleName());
			throw new RuntimeException(e);
		}
	}

	public static <T1 extends Model> List<Model> getRelations(Class<? extends OneToManyTypedRelation<T1>> relation, T1 entity) {
		if (entity.getId() == null)
			throw new IllegalArgumentException(entity.getClass().getSimpleName() + " is not saved to database yet, aborting");

		TableInfo crossTableInfo = Cache.getTableInfo(relation);
		Cursor cursor = Cache.openDatabase().rawQuery("SELECT entity2Type, entity2 FROM " + crossTableInfo.getTableName() + " WHERE entity1 = ?", new String[] {entity.getId().toString()});
		final List<Model> entities = new ArrayList<Model>();
		try {
			if (cursor.moveToFirst()) {
				do {
					String typeName = cursor.getString(0);
					@SuppressWarnings("unchecked")
					Class<? extends Model> entity2Class = (Class<? extends Model>) Class.forName(typeName);
					entities.add(Model.load(entity2Class, cursor.getLong(1)));
				}
				while (cursor.moveToNext());
			}
		}
		catch (Exception e) {
			Log.e("Failed to process cursor.", e);
			throw new RuntimeException(e);
		} finally {
			cursor.close();
		}

		return entities;
	}

	public OneToManyTypedRelation() {
		super();
	}
}
