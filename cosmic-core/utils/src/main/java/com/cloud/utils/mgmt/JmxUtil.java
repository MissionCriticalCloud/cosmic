//

//

package com.cloud.utils.mgmt;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

public class JmxUtil {
    public static ObjectName registerMBean(final ManagementBean mbean) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {

        return registerMBean(mbean.getName(), null, mbean);
    }

    public static ObjectName registerMBean(final String objTypeName, final String objInstanceName, final Object mbean) throws MalformedObjectNameException,
            InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException {

        String name = "com.cloud:type=" + objTypeName;
        if (objInstanceName != null && !objInstanceName.isEmpty()) {
            name += ", name=" + objInstanceName;
        }
        final ObjectName objectName = new ObjectName(name);

        final ArrayList<MBeanServer> server = MBeanServerFactory.findMBeanServer(null);
        if (server.size() > 0) {
            final MBeanServer mBeanServer = server.get(0);
            if (!mBeanServer.isRegistered(objectName)) {
                mBeanServer.registerMBean(mbean, objectName);
            }
            return objectName;
        } else {
            final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            if (!mBeanServer.isRegistered(objectName)) {
                mBeanServer.registerMBean(mbean, objectName);
            }
            return objectName;
        }
    }

    public static void unregisterMBean(final String objTypeName, final String objInstanceName) throws MalformedObjectNameException, MBeanRegistrationException,
            InstanceNotFoundException {

        final ObjectName name = composeMBeanName(objTypeName, objInstanceName);
        unregisterMBean(name);
    }

    private static ObjectName composeMBeanName(final String objTypeName, final String objInstanceName) throws MalformedObjectNameException {

        String name = "com.cloud:type=" + objTypeName;
        if (objInstanceName != null && !objInstanceName.isEmpty()) {
            name += ", name=" + objInstanceName;
        }

        return new ObjectName(name);
    }

    public static void unregisterMBean(final ObjectName name) throws MalformedObjectNameException, MBeanRegistrationException, InstanceNotFoundException {

        final ArrayList<MBeanServer> server = MBeanServerFactory.findMBeanServer(null);
        if (server.size() > 0) {
            final MBeanServer mBeanServer = server.get(0);
            mBeanServer.unregisterMBean(name);
        } else {
            final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            mBeanServer.unregisterMBean(name);
        }
    }
}
