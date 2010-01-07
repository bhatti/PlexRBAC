package com.plexobject.rbac.jmx.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.plexobject.rbac.jmx.ServiceJMXBean;
import com.plexobject.rbac.metric.Metric;
import com.plexobject.rbac.utils.TimeUtils;

public class ServiceJMXBeanImpl extends NotificationBroadcasterSupport
        implements ServiceJMXBean {
    private Map<String, String> properties = new ConcurrentHashMap<String, String>();
    private final String serviceName;
    private AtomicLong totalErrors;
    private AtomicLong totalRequests;

    private AtomicLong sequenceNumber;
    private String state;

    public ServiceJMXBeanImpl(final String serviceName) {
        this.serviceName = serviceName;
        this.totalErrors = new AtomicLong();
        this.totalRequests = new AtomicLong();
        this.sequenceNumber = new AtomicLong();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     */
    @Override
    public double getAverageElapsedTimeInNanoSecs() {
        return Metric.getMetric(getServiceName())
                .getAverageDurationInNanoSecs();
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public String getProperty(final String name) {
        return properties.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * java.lang.String)
     */
    @Override
    public void setProperty(final String name, final String value) {
        final String oldValue = properties.put(name, value);
        final Notification notification = new AttributeChangeNotification(this,
                sequenceNumber.incrementAndGet(), TimeUtils
                        .getCurrentTimeMillis(), name + " changed", name,
                "String", oldValue, value);
        sendNotification(notification);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public long getTotalDurationInNanoSecs() {
        return Metric.getMetric(getServiceName()).getTotalDurationInNanoSecs();
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public long getTotalErrors() {
        return totalErrors.get();
    }

    public void incrementError() {
        final long oldErrors = totalErrors.getAndIncrement();
        final Notification notification = new AttributeChangeNotification(this,
                sequenceNumber.incrementAndGet(), TimeUtils
                        .getCurrentTimeMillis(), "Errors changed", "Errors",
                "long", oldErrors, oldErrors + 1);
        sendNotification(notification);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public long getTotalRequests() {
        return totalRequests.get();
    }

    public void incrementRequests() {
        final long oldRequests = totalRequests.getAndIncrement();
        final Notification notification = new AttributeChangeNotification(this,
                sequenceNumber.incrementAndGet(), TimeUtils
                        .getCurrentTimeMillis(), "Requests changed",
                "Requests", "long", oldRequests, oldRequests + 1);
        sendNotification(notification);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE };
        String name = AttributeChangeNotification.class.getName();
        String description = "An attribute of this MBean has changed";
        MBeanNotificationInfo info = new MBeanNotificationInfo(types, name,
                description);
        return new MBeanNotificationInfo[] { info };
    }

    @Override
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ServiceJMXBeanImpl)) {
            return false;
        }
        ServiceJMXBeanImpl rhs = (ServiceJMXBeanImpl) object;
        return new EqualsBuilder().append(this.serviceName, rhs.serviceName)
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(
                this.serviceName).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serviceName", this.serviceName).append("totalErrors",
                        this.totalErrors).append("totalRequests",
                        this.totalRequests).append("totalRequests",
                        this.totalRequests).append("state", this.state).append(
                        "properties", this.properties).toString();
    }
}
