package com.plexobject.rbac;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Shahzad Bhatti
 * 
 */
public class Configuration {
    private static final String RESOURCE_NAME = "application.properties";
    private static final String CONFIG_DATABASE_KEY = "config.db.name";
    private static final String PAGE_SIZE = "page.size";
    private static final int MAX_PAGE_SIZE = 256;
    private static final String DEFAULT_CONFIG_DATABASE = "the_config";
    private static final Logger LOGGER = Logger.getLogger(Configuration.class);
    private static Configuration instance = new Configuration();

    private final Properties properties = new Properties();

    Configuration() {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(
                    RESOURCE_NAME);
            if (in == null) {
                throw new RuntimeException("Failed to find " + RESOURCE_NAME);
            }
            properties.load(in);
            properties.putAll(System.getProperties());

        } catch (IOException e) {
            LOGGER.error("Failed to load " + RESOURCE_NAME, e);
        }
        // user can override any property via command-line system-arguments

        properties.putAll(System.getProperties());

    }

    public static Configuration getInstance() {
        return instance;
    }

    public String getConfigDatabase() {
        return getProperty(CONFIG_DATABASE_KEY, DEFAULT_CONFIG_DATABASE);
    }

    public int getPageSize() {
        return getInteger(PAGE_SIZE, MAX_PAGE_SIZE);
    }

    public String getProperty(final String key) {
        return getProperty(key, null);
    }

    public String getProperty(final String key, final String def) {
        return properties.getProperty(key, def);
    }

    public int getInteger(final String key) {
        return getInteger(key, 0);
    }

    public int getInteger(final String key, final int def) {
        return Integer.parseInt(getProperty(key, String.valueOf(def)));
    }

    public double getDouble(final String key) {
        return getDouble(key, 0);
    }

    public double getDouble(final String key, final double def) {
        return Double.valueOf(getProperty(key, String.valueOf(def)))
                .doubleValue();
    }

    public boolean getBoolean(final String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(final String key, final boolean def) {
        return Boolean.valueOf(getProperty(key, String.valueOf(def)))
                .booleanValue();
    }

    public long getLong(final String key) {
        return getLong(key, 0);
    }

    public long getLong(final String key, long def) {
        return Long.valueOf(getProperty(key, String.valueOf(def)));
    }

}
