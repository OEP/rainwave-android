package cc.rainwave.android.api.types;

public class RainwaveException extends Exception {
	private static final long serialVersionUID = -6112296581405318238L;

	private int mCode;
	private String mMessage;
	
	public RainwaveException(Error error) {
		mCode = error.code;
		mMessage = error.text;
	}
	
	public RainwaveException(int code, String message) {
	    mCode = code;
	    mMessage = message;
	}
	
	public String getMessage() {
		return mMessage;
	}
	
	public String toString() {
		return String.format("%s: %s (%d)", super.toString(), mMessage, mCode);
	}
}
