package com.plexobject.rbac.jmx.impl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.plexobject.rbac.jmx.Log4jJMXBean;

public class Log4jJMXBeanImpl implements Log4jJMXBean {
    private static final Logger LOGGER = Logger
            .getLogger(Log4jJMXBeanImpl.class);

    private final String objectName;

    public Log4jJMXBeanImpl() {
        objectName = "org.apache.log4j:type=Settings";
    }

    @Override
    public String getLoggingLevel(String category) {
        return Logger.getLogger(category).getLevel().toString();
    }

    @Override
    public void setLoggingLevel(String category, String level) {
        Logger.getLogger(category).setLevel(Level.toLevel(level));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Setting logging level for " + category + " to "
                    + level);
        }
    }

    /**
     * @return the objectName
     */
    public String getObjectName() {
        return objectName;
    }

}
