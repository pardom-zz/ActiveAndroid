package com.activeandroid;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.content.AsyncTaskLoader;

import com.activeandroid.query.From;

import java.util.List;

/**
 * The Class ModelLoader.
 * 
 * @param <T>
 *            the generic type
 */
public class ModelLoader<T extends Model> extends AsyncTaskLoader<List<T>> {

	/** The m data set observer. */
	private DataSetObserver mDataSetObserver;
	
	/** The m query. */
	private From mQuery;

	/** The m results. */
	private List<T> mResults;

	/**
	 * Instantiates a new model loader.
	 * 
	 * @param context
	 *            the context
	 * @param from
	 *            the from
	 */
	public ModelLoader(Context context, From from) {
		super(context);
		this.mQuery = from;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 * 
	 * @param toolData
	 *            the tool data
	 */
	@Override
	public void deliverResult(List<T> toolData) {
		if (this.isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
			if (toolData != null) {
				this.onReleaseResources(toolData);
			}
		}
		List<T> oldToolData = toolData;
		this.mResults = toolData;

		if (this.isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(toolData);
		}

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldToolData != null) {
			this.onReleaseResources(oldToolData);
		}
	}

	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 * 
	 * @return the list
	 */
	@Override
	public List<T> loadInBackground() {
		List<T> results = this.mQuery.execute();
		return results;
	}

	/**
	 * Handles a request to cancel a load.
	 * 
	 * @param toolData
	 *            the tool data
	 */
	@Override
	public void onCanceled(List<T> toolData) {
		super.onCanceled(toolData);
		// At this point we can release the resources
		this.onReleaseResources(toolData);
	}

	/**
	 * On release resources.
	 * 
	 * @param toolData
	 *            the tool data
	 */
	protected void onReleaseResources(List<T> toolData) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		this.onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (this.mResults != null) {
			this.onReleaseResources(this.mResults);
			this.mResults = null;
		}

		// Stop monitoring for changes.
		if (this.mDataSetObserver != null) {
			Model.unregisterDataSetObserver(mQuery.getModelType(), this.mDataSetObserver);
			this.mDataSetObserver = null;
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (this.mResults != null) {
			// If we currently have a result available, deliver it
			// immediately.
			this.deliverResult(this.mResults);
		}

		// Start watching for changes in the job data.
		if (this.mDataSetObserver == null) {
			this.mDataSetObserver = new DataSetObserver() {
				@Override
				public void onChanged() {
					super.onChanged();
					
					/*
					 * It's always a freakin' threading issue, ain't it?
					 * Directly calling onContentChanged here doesn't seem to
					 * consistently work, but posting it to the main thread does.
					 */
					
					// Get a handler that can be used to post to the main thread
					Handler mainHandler = new Handler(getContext().getMainLooper());

					Runnable myRunnable = new Runnable() {
						@Override
						public void run() {
							ModelLoader.this.onContentChanged();
						}
					};
					
					mainHandler.post(myRunnable);

				}
			};

			Model.registerDataSetObserver(mQuery.getModelType(), this.mDataSetObserver);
		}

		if (this.takeContentChanged() || (this.mResults == null)) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			this.forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		this.cancelLoad();
	}

}
