package com.plexobject.rbac.jmx;

import javax.management.MXBean;

/**
 * This interface is used for JMX configuration and monitoring.
 * 
 * 
 */
@MXBean
public interface ServiceJMXBean {
    /**
     * 
     * @return service name
     */
    String getServiceName();

    /**
     * 
     * @return total number of requests
     */
    long getTotalRequests();

    /**
     * 
     * @return total number of errors
     */
    long getTotalErrors();

    /**
     * 
     * @return total elapsed time for all method executions in nano-secs
     */
    long getTotalDurationInNanoSecs();

    /**
     * 
     * @return average elapsed time for all method executions in nano-secs
     */
    double getAverageElapsedTimeInNanoSecs();

    /**
     * 
     * @param name
     *            - name of property
     * @return property value
     */
    String getProperty(String name);

    /**
     * 
     * @param name
     * @param value
     */
    void setProperty(String name, String value);

    /**
     * 
     * @return state of the service
     */
    String getState();
}
