package com.plexobject.rbac.http;

import java.io.IOException;

import com.plexobject.rbac.domain.Tuple;

public interface RestClient {
	public static int OK = 200;
	public static int OK_MIN = 200;
	public static int OK_MAX = 299;
	public static int OK_CREATED = 201;
	public static int OK_ACCEPTED = 202;
	public static int REDIRECT_PERMANENTLY = 301;
	public static int REDIRECT_FOUND = 302;
	public static int CLIENT_ERROR_BAD_REQUEST = 400;
	public static int CLIENT_ERROR_UNAUTHORIZED = 401;
	public static int CLIENT_ERROR_FORBIDDEN = 403;
	public static int CLIENT_ERROR_NOT_FOUND = 404;
	public static int CLIENT_ERROR_TIMEOUT = 408;
	public static int CLIENT_ERROR_CONFLICT = 409;
	public static int CLIENT_ERROR_PRECONDITION = 412;
	public static int SERVER_INTERNAL_ERROR = 500;
	public static int SERVICE_UNAVAILABLE = 503;

	Tuple get(final String path) throws IOException;

	Tuple put(final String path, final String body) throws IOException;

	int delete(final String path) throws IOException;

	Tuple post(final String path, final String body) throws IOException;

}
