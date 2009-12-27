package com.plexobject.rbac.utils;

public class CurrentUserRequest {
    private static final ThreadLocal<String> username = new ThreadLocal<String>();
    private static final ThreadLocal<String> ipAddress = new ThreadLocal<String>();

    public static void startRequest(final String username,
            final String ipAddress) {
        CurrentUserRequest.username.set(username);
        CurrentUserRequest.ipAddress.set(ipAddress);
    }

    public static void endRequest() {
        CurrentUserRequest.username.set(null);
        CurrentUserRequest.ipAddress.set(null);
    }

    public static String getUsername() {
        return username.get();
    }

    public static String getIPAddress() {
        return ipAddress.get();
    }
}
