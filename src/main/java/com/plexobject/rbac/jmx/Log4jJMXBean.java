package com.plexobject.rbac.jmx;

import javax.management.MXBean;

/**
 * This interface is used to configure log4j using JMX configuration
 * 
 * 
 */
@MXBean
public interface Log4jJMXBean {
    /**
     * 
     * @param category
     * @return level for logging
     */
    public String getLoggingLevel(String category);

    /**
     * sets logging level
     * 
     * @param category
     * @param level
     */
    public void setLoggingLevel(String category, String level);
}
