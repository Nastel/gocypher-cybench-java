/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.environment.services.CollectSystemInformation;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;

public class CollectPropertiesRun {
    private static final String GC_BEAN_NAME = "java.lang:type=GarbageCollector,name=PS MarkSweep";
    private static volatile GarbageCollectorMXBean gcMBean;

    private static final Logger LOG = LoggerFactory.getLogger(CollectSystemInformation.class);
    static HardwareProperties hardwareProp = new HardwareProperties();
    static JVMProperties jvmProperties = new JVMProperties();
    private static final DecimalFormat df = new DecimalFormat("#.####");
    private static final String[] excludeWindowsMACs = { "virtual", "hyper-v", "npcap" };

    public static void main(String[] args) throws Exception {
        getGCInfo(args);
        getHardwarePropsInfo(args);
    }

    private static void getGCInfo(String[] args) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> names = mbs.queryNames(null, null);
            System.out.println(names.toString().replace(", ", System.getProperty("line.separator")));
            List<java.lang.management.GarbageCollectorMXBean> gcMxBeans = ManagementFactory
                    .getGarbageCollectorMXBeans();
            for (java.lang.management.GarbageCollectorMXBean gcMxBean : gcMxBeans) {
                System.out.println(gcMxBean.getName());
                System.out.println(gcMxBean.getObjectName());
            }
            printGCInfo();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    private static void getHardwarePropsInfo(String[] args) {
        LOG.info("--- Main Started successfully ---");
        LOG.info("---------------------------------------------------------------------------");
        LOG.info("");
        int index;
        for (String commands : args) {
            switch (commands) {
            case "jvmPropertiesToFile":
                CollectSystemInformation.getJavaVirtualMachineProperties();
                CollectSystemInformation.outputHardwareDataObjectToFile(jvmProperties, "JVMProperties.txt");
                break;
            case "hardwarePropertiesToFile":
                CollectSystemInformation.getEnvironmentProperties();
                CollectSystemInformation.outputHardwareDataObjectToFile(hardwareProp, "HardwareProperties.txt");
                break;
            case "jvmPropertiesToFile=":
                index = Collections.singletonList(commands).indexOf("jvmPropertiesToFile=*");
                CollectSystemInformation.getJavaVirtualMachineProperties();
                CollectSystemInformation.outputHardwareDataObjectToFile(jvmProperties, args[index + 1]);
                break;
            case "hardwarePropertiesToFile=":
                index = Collections.singletonList(commands).indexOf("hardwarePropertiesToFile=*");
                CollectSystemInformation.getEnvironmentProperties();
                CollectSystemInformation.outputHardwareDataObjectToFile(hardwareProp, args[index + 1]);
                break;
            default:
                LOG.info("No additional output selected");
            }
        }
    }

    public CollectPropertiesRun() {
    }

    // initialize the GC MBean field
    private static void initGCMBean() {
        if (gcMBean == null) {
            synchronized (CollectPropertiesRun.class) {
                if (gcMBean == null) {
                    gcMBean = getGCMBean();
                }
            }
        }
    }

    // get the GarbageCollectorMXBean MBean from the
    // platform MBean server
    private static GarbageCollectorMXBean getGCMBean() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            GarbageCollectorMXBean bean = ManagementFactory.newPlatformMXBeanProxy(server, GC_BEAN_NAME,
                    GarbageCollectorMXBean.class);
            return bean;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    static boolean printGCInfo() {
        // initialize GC MBean
        List<String> test = new ArrayList<>();
        test.add("test1");
        test.add("test1");
        test.add("test1");
        test.add("test1");
        test.add("test1");
        test = null;
        System.gc();
        System.runFinalization();
        initGCMBean();
        try {
            GcInfo gci = gcMBean.getLastGcInfo();
            if (gci != null) {
                long id = gci.getId();
                long startTime = gci.getStartTime();
                long endTime = gci.getEndTime();
                long duration = gci.getDuration();
                if (startTime == endTime) {
                    return false; // no gc
                }
                System.out.println("GC ID: " + id);
                System.out.println("Start Time: " + startTime);
                System.out.println("End Time: " + endTime);
                System.out.println("Duration: " + duration);
                System.out.println("Before GC Memory Usage Details....");
                printMemUse(gci.getMemoryUsageBeforeGc());
                System.out.println("After GC Memory Usage Details....");
                printMemUse(gci.getMemoryUsageAfterGc());
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
        return true;
    }

    private static void printMemUse(Map<String, MemoryUsage> memMap) {
        for (Map.Entry<String, MemoryUsage> me : memMap.entrySet()) {
            System.out.println(me.getKey());
            MemoryUsage mu = me.getValue();
            System.out.print("Initial Size: " + mu.getInit());
            System.out.print(" Used: " + mu.getUsed());
            System.out.print(" Max: " + mu.getMax());
            System.out.print(" Committed: " + mu.getCommitted());
            System.out.println(" ");
        }
    }
}