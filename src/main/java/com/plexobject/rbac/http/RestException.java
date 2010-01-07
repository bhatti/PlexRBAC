package com.plexobject.rbac.http;

/**
 * This class is thrown when error occurs while making an HTTP request.
 *
 *
 */
public class RestException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int errorCode;
	public RestException(final String message) {
		super(message);
	}
    public RestException(final String message, final int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	public RestException(final Throwable error) {
		super(error);
	}
    public RestException(final Throwable error, final int errorCode) {
		super(error);
		this.errorCode = errorCode;
	}

    public RestException(final String message, final Throwable error) {
		super(message, error);
	}
    public RestException(final String message, final Throwable error, final int errorCode) {
		super(message, error);
		this.errorCode = errorCode;
	}
	public int getErrorCode() {
		return errorCode;
	}
}
