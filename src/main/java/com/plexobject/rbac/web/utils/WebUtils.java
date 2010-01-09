package com.plexobject.rbac.web.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;
import com.plexobject.rbac.domain.Domain;
import com.plexobject.rbac.utils.PasswordUtils;

public class WebUtils {
    private static final Logger LOGGER = Logger.getLogger(WebUtils.class);
    private static final String SESSION = "session";
    private static final String USERNAME = "username";
    private static final String DOMAIN = "domain";

    private static final int SESSION_EXPIRATION_IN_MINUTES = Configuration
            .getInstance().getInteger("session_expiration_in_minutes", 60 * 24); // 24-HOURS
    private static final boolean SECURED_SESSION = Configuration.getInstance()
            .getBoolean("secured_session_cookie");
    private static final String SESSION_COOKIE_DOMAIN = Configuration
            .getInstance().getProperty("session_cookie_domain");

    public static NewCookie[] createSession(final String domain,
            final String username) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain not specified");
        }
        if (GenericValidator.isBlankOrNull(username)) {
            throw new IllegalArgumentException("username not specified");
        }
        long expiresAt = System.currentTimeMillis()
                + (SESSION_EXPIRATION_IN_MINUTES * 60 * 1000);
        String session = PasswordUtils.encrypt(domain + ":" + username + ":"
                + expiresAt);
        return new NewCookie[] {
                new NewCookie(SESSION, session, "/", SESSION_COOKIE_DOMAIN,
                        domain + ":" + username,
                        SESSION_EXPIRATION_IN_MINUTES * 60, SECURED_SESSION),
                new NewCookie(USERNAME, username, "/", SESSION_COOKIE_DOMAIN,
                        domain + ":" + username,
                        SESSION_EXPIRATION_IN_MINUTES * 60, SECURED_SESSION),
                new NewCookie(DOMAIN, domain, "/", SESSION_COOKIE_DOMAIN,
                        domain + ":" + username,
                        SESSION_EXPIRATION_IN_MINUTES * 60, SECURED_SESSION) };
    }

    public static String getDomain(HttpServletRequest req) {
        String domain = getCookieValue(req, DOMAIN);
        if (domain != null) {
            return domain;
        }
        String uri = req.getRequestURI();
        String[] t = uri.split("/");
        if (uri.startsWith("/security/")) {
            return t[3];
        }
        return Domain.DEFAULT_DOMAIN_NAME;
    }

    public static boolean verifySession(Cookie[] cookies) {
        try {
            if (cookies == null || cookies.length < 3) {
                return false;
            }
            String domain = null;
            String username = null;
            String encryptedSession = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(DOMAIN)) {
                    domain = cookie.getValue();
                } else if (cookie.getName().equals(USERNAME)) {
                    username = cookie.getValue();
                } else if (cookie.getName().equals(SESSION)) {
                    encryptedSession = cookie.getValue();
                }
            }
            if (GenericValidator.isBlankOrNull(domain)
                    || GenericValidator.isBlankOrNull(username)
                    || GenericValidator.isBlankOrNull(encryptedSession)) {
                return false;
            }
            final String session = PasswordUtils.decrypt(encryptedSession);
            String[] t = session.split(":");
            if (t.length != 3) {
                return false;
            }
            long expiresAt = Long.valueOf(t[2]);
            return domain.equals(t[0]) && username.equals(t[1])
                    && expiresAt < System.currentTimeMillis();
        } catch (Exception e) {
            LOGGER.error("failed to verify session", e);
            return false;
        }
    }

    public static String getUser(HttpServletRequest req) {
        return getCookieValue(req, USERNAME);
    }

    public static String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
