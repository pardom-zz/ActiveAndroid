package com.activeandroid;

import java.util.List;

import com.activeandroid.util.Log;

/**
 * The Class ModelResponseHandler.
 */
public abstract class ModelResponseHandler<T extends Model> {

	/**
	 * On error.
	 * 
	 * @param errorMessage
	 *            the error message
	 * @param throwable
	 *            the throwable
	 */
	public void onError(String errorMessage, Throwable throwable) {
		Log.e("ModelResponseHandler onError: "
				+ errorMessage, throwable);
	}

	/**
	 * On success with multiple models.
	 * 
	 * @param models
	 *            the models
	 */
	public void onSuccess(List<T> models) {
		Log.v("ModelResponseHandler onSuccess: " + models.toString());
	}

	/**
	 * On success.
	 * 
	 * @param model
	 *            the model
	 */
	public void onSuccess(T model) {
		Log.v("ModelResponseHandler onSuccess: " + model.toString());
	}

}