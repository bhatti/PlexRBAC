package com.plexobject.rbac.jmx;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMRuntimeException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

import com.plexobject.rbac.jmx.impl.Log4jJMXBeanImpl;
import com.plexobject.rbac.jmx.impl.ServiceJMXBeanImpl;

/**
 * This class registers service-mbeans to the local mbean server Use following
 * system arguments -Dcom.sun.management.jmxremote.port=<port> \
 * -Dcom.sun.management.jmxremote.authenticate=false \
 * -Dcom.sun.management.jmxremote.ssl=false
 * 
 */
public class JMXRegistrar {
    private static final Logger LOGGER = Logger.getLogger(JMXRegistrar.class);
    private MBeanServer mbeanServer;
    private static final JMXRegistrar INSTANCE = new JMXRegistrar();
    private Map<String, ServiceJMXBeanImpl> mbeans = new ConcurrentHashMap<String, ServiceJMXBeanImpl>();

    public static JMXRegistrar getInstance() {
        return INSTANCE;
    }

    /**
     * 
     * @param serviceClass
     *            -- service class
     * @return instance of ServiceJMXBean
     */
    public ServiceJMXBeanImpl register(final Class<?> serviceClass) {
        return register(serviceClass.getPackage().getName() + ":type="
                + serviceClass.getSimpleName());
    }

    public Collection<ServiceJMXBeanImpl> getServiceJMXBeans() {
        return Collections.unmodifiableCollection(mbeans.values());
    }

    /**
     * 
     * @param serviceName
     *            -- name of service (that is also used as object name)
     * @return instance of ServiceJMXBean
     */
    public ServiceJMXBeanImpl register(final String serviceName) {
        if (GenericValidator.isBlankOrNull(serviceName)) {
            throw new IllegalArgumentException("serviceName not specified");
        }
        synchronized (serviceName.intern()) {
            ServiceJMXBeanImpl mbean = mbeans.get(serviceName);
            if (mbean == null) {
                mbean = new ServiceJMXBeanImpl(serviceName);
                mbeans.put(serviceName, mbean);
                try {
                    ObjectName name = new ObjectName(serviceName);
                    mbeanServer.registerMBean(mbean, name);

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("regstered MXBean " + serviceName);
                    }
                } catch (InstanceAlreadyExistsException e) {
                } catch (Exception e) {
                    throw new JMRuntimeException("Failed to register "
                            + serviceName + " due to " + e);
                }
            }
            return mbean;
        }
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JMXRegistrar)) {
            return false;
        }
        JMXRegistrar rhs = (JMXRegistrar) object;
        return new EqualsBuilder().append(this.mbeanServer, rhs.mbeanServer)
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(786529047, 1924536713).append(mbeanServer)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mbeanServer", this.mbeanServer).toString();
    }

    JMXRegistrar() {
        mbeanServer = ManagementFactory.getPlatformMBeanServer();
        if (mbeanServer == null) {
            List<MBeanServer> existingServers = MBeanServerFactory
                    .findMBeanServer(null);

            if (!existingServers.isEmpty()) {
                mbeanServer = existingServers.get(0);
            } else {
                mbeanServer = MBeanServerFactory.createMBeanServer();
            }
        }
        // registering log4j mbean
        final Log4jJMXBeanImpl mbean = new Log4jJMXBeanImpl();
        ObjectName name;
        try {
            name = new ObjectName(mbean.getObjectName());
            mbeanServer.registerMBean(mbean, name);
        } catch (Exception e) {
            LOGGER.error("Failed to register log4j-mbean", e);
        }

    }

}
