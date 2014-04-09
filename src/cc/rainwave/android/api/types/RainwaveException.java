package cc.rainwave.android.api.types;

public class RainwaveException extends Exception {
    private static final long serialVersionUID = -6112296581405318238L;

    private int mStatusCode = -1;

    private int mErrorCode = -1;

    public RainwaveException(String message) {
        super(message);
    }

    public RainwaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public RainwaveException(String message, int httpResponse) {
        this(message, httpResponse, -1);
    }

    public RainwaveException(String message, int httpResponse, int errorCode) {
        this(message, httpResponse, errorCode, null);
    }

    public RainwaveException(String message, int httpResponse, int errorCode, Throwable cause) {
        super(message, cause);
        mStatusCode = httpResponse;
        mErrorCode = errorCode;
    }

    /**
     * Returns the HTTP status code issued. A value of -1 indicates an invalid
     * or unknown response code.
     * @return HTTP status code
     */
    public int getStatusCode() {
        return mStatusCode;
    }

    /**
     * Returns the error code issued by the backend server. A value of -1 indicates an invalid
     * or unknown response code.
     * @return
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /** Indicates an unknown status code. */
    public static final int STATUS_UNKNOWN = -1;

    /** Indicates an unknown error code. */
    public static final int    ERROR_UNKNOWN = -1;
}
