package com.plexobject.rbac.web.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

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
import com.plexobject.rbac.domain.Tuple;
import com.plexobject.rbac.utils.PasswordUtils;

public class WebUtils {
    private static final Logger LOGGER = Logger.getLogger(WebUtils.class);
    private static final String SESSION = "session";

    private static final int SESSION_EXPIRATION_IN_MINUTES = Configuration
            .getInstance().getInteger("session_expiration_in_minutes", 60 * 24); // 24-HOURS
    private static final boolean SECURED_SESSION = Configuration.getInstance()
            .getBoolean("secured_session_cookie");
    private static final String SESSION_COOKIE_DOMAIN = Configuration
            .getInstance().getProperty("session_cookie_domain");

    public static NewCookie createSessionCookie(final String domain,
            final String subjectName) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, UnsupportedEncodingException {
        if (GenericValidator.isBlankOrNull(domain)) {
            throw new IllegalArgumentException("domain not specified");
        }
        if (GenericValidator.isBlankOrNull(subjectName)) {
            throw new IllegalArgumentException("subjectName not specified");
        }
        long expiresAt = System.currentTimeMillis()
                + (SESSION_EXPIRATION_IN_MINUTES * 60 * 1000);
        String session = createSession(domain, subjectName, expiresAt);
        return new NewCookie(SESSION, session, "/", SESSION_COOKIE_DOMAIN,
                domain + ":" + subjectName, SESSION_EXPIRATION_IN_MINUTES * 60,
                SECURED_SESSION);
    }

    public static String getDomain(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String[] t = uri.split("/");
        if (uri.startsWith("/api/security/") && t.length > 4) { // /api/security/login/domain
            return t[4];
        }
        Tuple domainAndSubject = verifySession(req);
        if (domainAndSubject != null) {
            return domainAndSubject.first();
        }
        return Domain.DEFAULT_DOMAIN_NAME;
    }

    public static Tuple verifySession(HttpServletRequest req) {
        try {
            String b64Session = getCookieValue(req, SESSION);

            return verifySession(b64Session);
        } catch (Exception e) {
            LOGGER.error("failed to verify session", e);
            return null;
        }
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

    private static String createSession(final String domain,
            final String subjectName, long expiresAt)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, UnsupportedEncodingException {
        String payload = domain + ":" + subjectName + ":" + expiresAt;
        String ecnrypted = PasswordUtils.encrypt(payload);
        return PasswordUtils.byteToBase64(ecnrypted.getBytes());
    }

    private static Tuple verifySession(String b64Session) throws IOException,
            NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeySpecException {
        if (!GenericValidator.isBlankOrNull(b64Session)) { 
            final String encryptedSession = new String(PasswordUtils
                    .base64ToByte(b64Session));
            final String session = PasswordUtils.decrypt(encryptedSession);
            String[] t = session.split(":");
            if (t.length == 3) {
                long expiresAt = Long.valueOf(t[2]);
                if (expiresAt >= System.currentTimeMillis()) {
                    return new Tuple(t[0], t[1]);
                }
            }
        }
        return null;
    }

}
