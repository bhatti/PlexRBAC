package com.plexobject.rbac.utils;

public class CurrentUserRequest {
    private static final ThreadLocal<String> domain = new ThreadLocal<String>();
    private static final ThreadLocal<String> username = new ThreadLocal<String>();
    private static final ThreadLocal<String> ipAddress = new ThreadLocal<String>();

    public static void startRequest(final String domain, final String username,
            final String ipAddress) {
        CurrentUserRequest.domain.set(domain);
        CurrentUserRequest.username.set(username);
        CurrentUserRequest.ipAddress.set(ipAddress);
    }

    public static void endRequest() {
        CurrentUserRequest.domain.set(null);
        CurrentUserRequest.username.set(null);
        CurrentUserRequest.ipAddress.set(null);
    }

    public static String getDomain() {
        return domain.get();
    }

    public static String getUsername() {
        return username.get();
    }

    public static String getIPAddress() {
        return ipAddress.get();
    }
}
