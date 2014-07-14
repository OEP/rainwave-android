/*
 * Copyright (c) 2013, Paul M. Kilgo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Paul Kilgo nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

    public String getMessage() {
        StringBuffer buffer = new StringBuffer();
        if(getStatusCode() != STATUS_UNKNOWN) {
            buffer.append("HTTP ");
            buffer.append(getStatusCode());
            buffer.append(": ");
        }
        String message = super.getMessage();
        if(message != null) {
            buffer.append(message);
        }
        if(getErrorCode() != ERROR_UNKNOWN) {
            buffer.append("[errno ");
            buffer.append(getErrorCode());
            buffer.append("]");
        }
        return buffer.toString();
    }

    /** Indicates an unknown status code. */
    public static final int STATUS_UNKNOWN = -1;

    /** Indicates an unknown error code. */
    public static final int ERROR_UNKNOWN = -1;
}
