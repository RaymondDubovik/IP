package com.fergus.esa;

import android.os.AsyncTask;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 29/01/2016
 */
public abstract class ErrorAsyncTask<T1, T2, T3> extends AsyncTask<T1, T2, T3>{
	private boolean error = false;


	public boolean hasError() {
		return error;
	}


	public ErrorAsyncTask setError(boolean error) {
		this.error = error;
		return this;
	}
}
