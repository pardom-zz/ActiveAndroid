package com.activeandroid.widget;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.activeandroid.Model;

public class ModelAdapter<T extends Model> extends ArrayAdapter<T> {

	public ModelAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public ModelAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}

	public ModelAdapter(Context context, int textViewResourceId,
			List<T> objects) {
		super(context, textViewResourceId, objects);
	}

	public ModelAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	/**
	 * Clears the adapter and, if data != null, fills if with new Items.
	 * 
	 * @param data A List<T> which members get added to the adapter.
	 */
	public void setData(List<T> data) {
	    clear();
	    if (data != null) {
	        for (T t : data) {
				add(t);
			}
	    }
	}

	/**
	 * @throws RuntimeException If no record is found.
	 * @return The Id of the record at position.
	 */
	@Override
	public long getItemId(int position) {
		T t = this.getItem(position);
		if (t!=null)
			return t.getId();
		else
			throw new RuntimeException("ItemNotfound");
	}

}
