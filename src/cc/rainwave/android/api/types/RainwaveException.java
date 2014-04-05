package cc.rainwave.android.api.types;

import android.content.Context;

public class RainwaveException extends Exception {
	private static final long serialVersionUID = -6112296581405318238L;

	private Context mContext;
	
	private int mResId;
	
	private String mMessage;
	
	public RainwaveException(int code, String message) {
	    mMessage = message;
	}
	
	public String getMessage() {
		return (mContext != null) ? mContext.getResources().getString(mResId) : mMessage;
	}
	
	public String toString() {
		return String.format("%s: %s", super.toString(), getMessage());
	}
}
