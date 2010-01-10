package com.plexobject.rbac.utils;

public class CurrentRequest {
    private static final ThreadLocal<String> domain = new ThreadLocal<String>();
    private static final ThreadLocal<String> subjectname = new ThreadLocal<String>();
    private static final ThreadLocal<String> ipAddress = new ThreadLocal<String>();

    public static void startRequest(final String domain, final String subjectname,
            final String ipAddress) {
        CurrentRequest.domain.set(domain);
        CurrentRequest.subjectname.set(subjectname);
        CurrentRequest.ipAddress.set(ipAddress);
    }

    public static void setSubjectname(String subjectname) {
        CurrentRequest.subjectname.set(subjectname);
    }

    public static void endRequest() {
        CurrentRequest.domain.set(null);
        CurrentRequest.subjectname.set(null);
        CurrentRequest.ipAddress.set(null);
    }

    public static String getDomain() {
        return domain.get();
    }

    public static String getSubjectname() {
        return subjectname.get();
    }

    public static String getIPAddress() {
        return ipAddress.get();
    }
}
