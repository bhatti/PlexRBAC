package com.plexobject.rbac.jmx;

import java.io.Closeable;

import javax.management.JMRuntimeException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.plexobject.rbac.Configuration;

public class JMXRemoteConnector implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(JMXRegistrar.class);
    private final String url;
    private JMXConnector connector;
    private MBeanServerConnection serverConnection;
    private static JMXRemoteConnector INSTANCE = new JMXRemoteConnector();

    public static JMXRemoteConnector getInstance() {
        return INSTANCE;
    }

    public <T> T getProxy(final String service, final Class<T> clazz) {
        try {
            final ObjectName mxbeanName = new ObjectName(service);
            return JMX.newMXBeanProxy(serverConnection, mxbeanName, clazz);
        } catch (Exception e) {
            throw new JMRuntimeException("Failed to get proxy for " + service
                    + " due to " + e);
        }
    }

    synchronized void connect() {
        if (isConnected()) {
            return;
        }

        try {
            JMXServiceURL serviceUrl = new JMXServiceURL(url);
            connector = JMXConnectorFactory.connect(serviceUrl);
            serverConnection = connector.getMBeanServerConnection();
        } catch (Exception e) {
            throw new JMRuntimeException("Failed to connect to " + url
                    + " due to " + e);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Connected to remote JMX server " + url);
        }
    }

    @Override
    public synchronized void close() {
        if (!isConnected()) {
            return;
        }

        try {
            connector.close();
        } catch (Exception e) {
        }

        connector = null;
        serverConnection = null;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Closed remote connection to JMX server " + url);
        }
    }

    synchronized boolean isConnected() {
        return connector != null;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JMXRemoteConnector)) {
            return false;
        }
        JMXRemoteConnector rhs = (JMXRemoteConnector) object;
        return new EqualsBuilder().append(this.url, rhs.url).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(url)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("url", this.url).toString();
    }

    JMXRemoteConnector() {
        this(Configuration.getInstance().getProperty("weseed.jmx.rmi.host",
                "localhost"), Configuration.getInstance().getInteger(
                "weseed.jmx.rmi.port", 1099));
    }

    JMXRemoteConnector(final String host, final int port) {
        this.url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port
                + "/jmxrmi";

    }

}
