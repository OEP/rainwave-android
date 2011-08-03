package cc.rainwave.android.api.types;

public class RainwaveException extends Exception {
	private static final long serialVersionUID = -6112296581405318238L;

	private Error mError;
	
	public RainwaveException(Error error) {
		mError = error;
	}
	
	public String getMessage() {
		return mError.text;
	}
	
	public String toString() {
		return super.toString() + " " + mError.text;
	}
}
