package com.plexobject.rbac.utils;

public class CurrentRequest {
    private static final ThreadLocal<String> domain = new ThreadLocal<String>();
    private static final ThreadLocal<String> subjectName = new ThreadLocal<String>();
    private static final ThreadLocal<String> ipAddress = new ThreadLocal<String>();

    public static void startRequest(final String domain, final String subjectName,
            final String ipAddress) {
        CurrentRequest.domain.set(domain);
        CurrentRequest.subjectName.set(subjectName);
        CurrentRequest.ipAddress.set(ipAddress);
    }

    public static void setSubjectName(String subjectName) {
        CurrentRequest.subjectName.set(subjectName);
    }

    public static void endRequest() {
        CurrentRequest.domain.set(null);
        CurrentRequest.subjectName.set(null);
        CurrentRequest.ipAddress.set(null);
    }

    public static String getDomain() {
        return domain.get();
    }

    public static String getSubjectName() {
        return subjectName.get();
    }

    public static String getIPAddress() {
        return ipAddress.get();
    }
}
