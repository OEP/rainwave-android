package cc.rainwave.android.api.types;

import android.content.Context;
import android.content.res.Resources;

public class RainwaveException extends Exception {
	private static final long serialVersionUID = -6112296581405318238L;

	private Context mContext;
	
	private int mCode;
	
	private int mResId;
	
	private String mMessage;
	
	public RainwaveException(GenericResult error) {
		mCode = error.code;
		mMessage = error.text;
	}
	
	public RainwaveException(int code, String message) {
	    mCode = code;
	    mMessage = message;
	}
	
	public int getCode() {
		return mCode;
	}
	
	public String getMessage() {
		return (mContext != null) ? mContext.getResources().getString(mResId)
				: mMessage;
	}
	
	public String toString() {
		return String.format("%s: %s (%d)", super.toString(), getMessage(), mCode);
	}
}
