import com.gocypher.benchmarks.runner.environment.model.HardwareProperties;
import com.gocypher.benchmarks.runner.environment.model.JVMProperties;
import com.gocypher.benchmarks.runner.environment.services.CollectSystemInformation;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.*;

public class CollectPropertiesRun {
    private static final String GC_BEAN_NAME =
            "java.lang:type=GarbageCollector,name=PS MarkSweep";
    private static volatile GarbageCollectorMXBean gcMBean;

    private static final Logger LOG = LoggerFactory.getLogger(CollectSystemInformation.class);
    static HardwareProperties hardwareProp = new HardwareProperties();
    static JVMProperties jvmProperties = new JVMProperties();
    private static final DecimalFormat df = new DecimalFormat("#.####");
    private static final String[] excludeWindowsMACs = {"virtual", "hyper-v", "npcap"};

    public static void main (String [] args)throws Exception{
        getGCInfo(args);
        getHardwarePropsInfo(args);
    }

    private static void getGCInfo(String [] args){
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set names = mbs.queryNames(null, null);
            System.out.println(names.toString().replace(", ", System.getProperty("line.separator")));
            List<java.lang.management.GarbageCollectorMXBean> gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
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
    private static void getHardwarePropsInfo(String [] args){
        LOG.info ("--- Main Started successfully ---") ;
        LOG.info ("---------------------------------------------------------------------------") ;
        LOG.info ("") ;
        int index;
        for (String commands: args) {
            switch(commands) {
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
                    CollectSystemInformation.outputHardwareDataObjectToFile(jvmProperties, args[index+1]);
                    break;
                case "hardwarePropertiesToFile=":
                    index = Collections.singletonList(commands).indexOf("hardwarePropertiesToFile=*");
                    CollectSystemInformation.getEnvironmentProperties();
                    CollectSystemInformation.outputHardwareDataObjectToFile(hardwareProp, args[index+1]);
                    break;
                default:
                    LOG.info ("No additional output selected") ;
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
            GarbageCollectorMXBean bean =
                    ManagementFactory.newPlatformMXBeanProxy(server,
                            GC_BEAN_NAME, GarbageCollectorMXBean.class);
            return bean;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    static boolean printGCInfo() {
        // initialize GC MBean
        List<String>  test = new ArrayList<>();
        test.add("test1");
        test.add("test1");
        test.add("test1");
        test.add("test1");
        test.add("test1");
        test = null;
        System.gc ();
        System.runFinalization ();
        initGCMBean();
        try {
            GcInfo gci = gcMBean.getLastGcInfo();
            if (gci != null) {
                long id = gci.getId();
                long startTime = gci.getStartTime();
                long endTime = gci.getEndTime();
                long duration = gci.getDuration();
                if (startTime == endTime) {
                    return false;   // no gc
                }
                System.out.println("GC ID: " + id);
                System.out.println("Start Time: " + startTime);
                System.out.println("End Time: " + endTime);
                System.out.println("Duration: " + duration);
                Map mapBefore = gci.getMemoryUsageBeforeGc();
                Map mapAfter = gci.getMemoryUsageAfterGc();
                System.out.println("Before GC Memory Usage Details....");
                Set memType = mapBefore.keySet();
                Iterator it = memType.iterator();
                while (it.hasNext()) {
                    String type = (String) it.next();
                    System.out.println(type);
                    MemoryUsage mu1 = (MemoryUsage) mapBefore.get(type);
                    System.out.print("Initial Size: " + mu1.getInit());
                    System.out.print(" Used: " + mu1.getUsed());
                    System.out.print(" Max: " + mu1.getMax());
                    System.out.print(" Committed: " + mu1.getCommitted());
                    System.out.println(" ");
                }
                System.out.println("After GC Memory Usage Details....");
                memType = mapAfter.keySet();
                it = memType.iterator();
                while (it.hasNext()) {
                    String type = (String) it.next();
                    System.out.println(type);
                    MemoryUsage mu2 = (MemoryUsage) mapAfter.get(type);
                    System.out.print("Initial Size: " + mu2.getInit());
                    System.out.print(" Used: " + mu2.getUsed());
                    System.out.print(" Max: " + mu2.getMax());
                    System.out.print(" Committed: " + mu2.getCommitted());
                    System.out.println(" ");
                }
            }
        } catch(RuntimeException re){
            throw re;
        } catch(Exception exp){
            throw new RuntimeException(exp);
        }
        return true;
    }
}