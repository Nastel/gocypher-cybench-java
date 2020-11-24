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

package com.gocypher.cybench.launcher.environment.services;

import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.utils.Constants;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import org.jutils.jhardware.HardwareInfo;
import org.jutils.jhardware.model.BiosInfo;
import org.jutils.jhardware.model.MemoryInfo;
import org.jutils.jhardware.model.MotherboardInfo;
import org.jutils.jhardware.model.OSInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.gocypher.cybench.launcher.BenchmarkRunner.getProperty;

public class CollectSystemInformation {
    private static final Logger LOG = LoggerFactory.getLogger(CollectSystemInformation.class);
    private static final DecimalFormat df = new DecimalFormat("#.####");
    private static final String[] excludeWindowsMACs = {"virtual", "hyper-v", "npcap"};
    static HardwareProperties hardwareProp = new HardwareProperties();
    static JVMProperties jvmProperties = new JVMProperties();

    public static void main(String args[]) {
        getEnvironmentProperties();
    }

    /**
     * Collect and return all the main properties about the JVM
     */
    public static JVMProperties getJavaVirtualMachineProperties() {
        LOG.info("JVM memory properties...");
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        jvmProperties.setJvmMemoryInitialMB(
                Double.parseDouble(df.format(memoryMXBean.getHeapMemoryUsage().getInit() / 1048576)));
        jvmProperties.setJvmMemoryUsedHeapMB(
                Double.parseDouble(df.format(memoryMXBean.getHeapMemoryUsage().getUsed() / 1048576)));
        jvmProperties.setJvmMemoryMaxHeapMB(
                Double.parseDouble(df.format(memoryMXBean.getHeapMemoryUsage().getMax() / 1048576)));
        jvmProperties.setJvmMemoryCommittedMB(
                Double.parseDouble(df.format(memoryMXBean.getHeapMemoryUsage().getCommitted() / 1048576)));

        LOG.info("JVM system properties...");
        Properties properties = System.getProperties();
        for (Object property : properties.keySet()) {
            switch (property.toString()) {
                case "java.runtime.name":
                    jvmProperties.setJvmRuntimeName(properties.getProperty("java.runtime.name"));
                    break;
                case "sun.boot.library.path":
                    jvmProperties.setJvmUserBootLibraryPath(properties.getProperty("sun.boot.library.path"));
                    break;
                case "java.vendor.url":
                    jvmProperties.setJvmVendorURL(properties.getProperty("java.vendor.url"));
                    break;
                case "java.vendor":
                    jvmProperties.setJvmVendor(properties.getProperty("java.vendor"));
                    break;
                case "java.vm.name":
                    jvmProperties.setJvmVmName(properties.getProperty("java.vm.name"));
                    break;
                case "java.runtime.version":
                    jvmProperties.setJvmRuntimeVersion(properties.getProperty("java.runtime.version"));
                    break;
                case "java.home":
                    jvmProperties.setJvmUserJavaHome(properties.getProperty("java.home"));
                    break;
                case "user.language":
                    jvmProperties.setJvmUserLanguage(properties.getProperty("user.language"));
                    break;
                case "java.vm.info":
                    jvmProperties.setJvmVmInfo(properties.getProperty("java.vm.info"));
                    break;
                case "file.encoding":
                    jvmProperties.setJvmOtherEncoding(properties.getProperty("file.encoding"));
                    break;
                case "sun.io.unicode.encoding":
                    jvmProperties.setJvmOtherIOUnicodeEncoding(properties.getProperty("sun.io.unicode.encoding"));
                    break;
                case "java.vm.version":
                    jvmProperties.setJvmVmVersion(properties.getProperty("java.vm.version"));
                    break;
                case "sun.management.compiler":
                    jvmProperties.setJvmOtherManagementCompiler(properties.getProperty("sun.management.compiler"));
                    break;
                case "java.class.version":
                    jvmProperties.setJvmJavaClassVersion(properties.getProperty("java.class.version"));
                    break;
                case "java.specification.name":
                    jvmProperties.setJvmJavaSpecificationName(properties.getProperty("java.specification.name"));
                    break;
                case "java.version":
                    jvmProperties.setJvmJavaVersion(properties.getProperty("java.version"));
                    break;

            }
        }
        return jvmProperties;
    }

    /**
     * Collect properties about program JVm arguments and the running gc's at the
     * start of execution
     *
     * @return
     */
    public static Map<String, Object> getUnclassifiedProperties() {
        LOG.info("Unclassified JVM and GC properties...");
        Map<String, Object> unclassifiedProperties = new HashMap<>();
        List<String> inputArguments = new ArrayList<>(ManagementFactory.getRuntimeMXBean().getInputArguments());
        inputArguments.removeIf(inputArgument -> inputArgument.contains("-javaagent"));
        unclassifiedProperties.put("performanceJvmRuntimeParameters", inputArguments);

        List<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
        List<String> garbageCollectorsOutput = new ArrayList<>();
//        Map<String, Object> garbageCollectorsMap = new HashMap<>();
        for (GarbageCollectorMXBean garbageCollector : garbageCollectors) {
//            garbageCollectorsMap.put(garbageCollector.getName(), garbageCollector.getName()) ;
            garbageCollectorsOutput.add(garbageCollector.getName());
        }
        unclassifiedProperties.put("performanceGarbageCollectors", garbageCollectorsOutput);
        return unclassifiedProperties;
    }

    /**
     * Fill the needed hardware properties using the 3-rd party libraries and system
     * properties.
     */
    public static HardwareProperties getEnvironmentProperties() {
        if (!Boolean.parseBoolean(getProperty(Constants.COLLECT_HW))) return new HardwareProperties.EmptyHardwareProperties();
        SystemInfo sysProps = new SystemInfo();
        HardwareAbstractionLayer hwProps = sysProps.getHardware();
        try {
            getProcessorProperties(hwProps);
            geMemoryProperties(hwProps);
            getGraphicsCardProperties(hwProps);
            getLaunchDiskProperties(hwProps);
            getNetworkProperties();
            getOSProperties(sysProps);
        } catch (Exception e) {
            LOG.error(
                    "An error occurred on reading of com.gocypher.benchmarks.runner.environment properties: Some of the properties present in other reports might be missing");
            ;
        }
        try {
            getMemoryInfo();
            setBiosProperties();
            setMotherboardProperties(hwProps);
        } catch (Exception e) {
            LOG.error(
                    "An error occurred on reading of com.gocypher.benchmarks.runner.environment properties: Some of the properties present in other reports might be missing");
            ;
            setPropertiesForMacOS();
        }
        return hardwareProp;
    }

    /* OSHI take system environments properties */

    /**
     * Get Processor properties using OSHI
     */
    private static void getProcessorProperties(HardwareAbstractionLayer hwProps) throws Exception {
        LOG.info("Looking for Processor info OSHI...");
        CentralProcessor cpu = hwProps.getProcessor();
        String procSpeedInGHz = (double) cpu.getProcessorIdentifier().getVendorFreq() / 1000000.0 + " MHz";
        hardwareProp.setHwProcName(cpu.getProcessorIdentifier().getName());
        hardwareProp.setHwProcManufacturer(cpu.getProcessorIdentifier().getVendor());
        hardwareProp.setHwProcSpeed(procSpeedInGHz);
        hardwareProp.setHwProcMicroArchitecture(cpu.getProcessorIdentifier().getMicroarchitecture());
        hardwareProp.setHwProcCoresCount(cpu.getPhysicalProcessorCount());
        if (cpu.getLogicalProcessorCount() / cpu.getPhysicalProcessorCount() != 2) {
            hardwareProp.setHwProcLogicalThreadsCount(cpu.getPhysicalProcessorCount() * 2);
        } else {
            hardwareProp.setHwProcLogicalThreadsCount(cpu.getLogicalProcessorCount());
        }
    }

    /**
     * Get Memory properties using OSHI
     */
    private static void geMemoryProperties(HardwareAbstractionLayer hwProps) throws Exception {
        LOG.info("Looking for Memory properties OSHI...");
        GlobalMemory mem = hwProps.getMemory();
        hardwareProp.setHwMemAvailableMemoryBytes((double) mem.getAvailable());
        hardwareProp.setHwMemTotalMemoryBytes((double) mem.getTotal());
    }

    /**
     * Get GraphicsCard properties using OSHI
     */
    private static void getGraphicsCardProperties(HardwareAbstractionLayer hwProps) throws Exception {
        LOG.info("Looking for GraphicsCard properties OSHI...");
        List<oshi.hardware.GraphicsCard> graphicCardsProps = hwProps.getGraphicsCards();
        StringBuilder graphicCards = new StringBuilder();
        for (oshi.hardware.GraphicsCard card : graphicCardsProps) {
            if (graphicCards.length() > 1) {
                graphicCards.append(" , ");
            }
            graphicCards.append(card.getName());
        }
        hardwareProp.setHwGraphicCName(graphicCards.toString());
    }

    /**
     * Method that returns the launch disk sizes type and model. Includes
     * implementation for multiple OS types.
     *
     * @param hwProps
     * @throws Exception
     */
    private static void getLaunchDiskProperties(HardwareAbstractionLayer hwProps) throws Exception {
        LOG.info("Looking for Disk properties OSHI...");
        File root = null;
        root = new File(CollectSystemInformation.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        hardwareProp.setHwLaunchDiskTotalMemoryGB(((double) root.getTotalSpace() / 1073741824));
        hardwareProp.setHwLaunchDiskFreeMemoryGB(((double) root.getFreeSpace() / 1073741824));

        // Execute a command in PowerShell session
        Map<String, String> diskPropertyMap = new HashMap<>();
        String osName = System.getProperty("os.name");
        try {
            if (osName.toLowerCase().contains("windows")) {
                PowerShellResponse response = PowerShell
                        .executeSingleCommand("Get-PhysicalDisk | Select MediaType, SerialNumber");
                String[] responseLines = response.getCommandOutput().split("\\r?\\n");
                for (int i = 3; i < responseLines.length; i++) {
                    String[] responseProperties = responseLines[i].split("\\s+", 2);
                    diskPropertyMap.put(responseProperties[1].trim(), responseProperties[0].trim());
                }
            }
        } catch (Exception e) {
            LOG.info(
                    "There was a problem while trying to determine the running hard drive type. Will continue without this property.");
        }

        List<HWDiskStore> diskProperties = hwProps.getDiskStores();
        for (HWDiskStore disk : diskProperties) {
            List<HWPartition> partitions = disk.getPartitions();
            for (HWPartition partition : partitions) {
                if (root.toString().contains(partition.getMountPoint())) {
                    hardwareProp.setHwLaunchDiskModel(disk.getModel());
                    if (osName.toLowerCase().contains("mac os")) {
                        String type = getDiskModelAndType(disk.getName(), "'Solid State:'");
                        if (type.equals("Yes")) {
                            type = "SSD";
                        } else {
                            type = "HDD";
                        }
                        hardwareProp.setHwLaunchDiskType(type);
                    } else {
                        for (String model : diskPropertyMap.keySet()) {
                            String serialNumber = disk.getSerial().trim();
                            if (serialNumber.equals(model)) {
                                hardwareProp.setHwLaunchDiskType(diskPropertyMap.get(model));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get network properties using OSHI and filtering to get the correct mac
     * address for different operating machines. Excluding virtual machines macs
     */
    private static void getNetworkProperties() throws Exception {
        ;

        InetAddress ip = null;
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netw : Collections.list(nets)) {
            // Filter names for UNIX to take "eth0" , "en0"
            // Filter names for WINDOWS to not take "Virtual", "Hyper-V"
            String tempName = netw.toString().toLowerCase();
            if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                if (tempName.contains("eth0")
                        || Pattern.compile("(eth(\\w)*0(\\w)*)|(en(\\w)*0(\\w*))").matcher(tempName).find()) {
                    Enumeration<InetAddress> addresses = netw.getInetAddresses();
                    for (InetAddress addr : Collections.list(addresses)) {
                        if (addr instanceof Inet4Address) {
                            ip = addr;
                        }
                    }
                }
            } else if (!stringContainsItemFromList(tempName, excludeWindowsMACs)) {
                Enumeration<InetAddress> addresses = netw.getInetAddresses();
                for (InetAddress addr : Collections.list(addresses)) {
                    if (addr instanceof Inet4Address) {
                        if (!addr.toString().equals("/127.0.0.1")) {
                            ip = addr;
                        }
                    }
                }
            }
        }

        if (ip == null) {
            ip = InetAddress.getLocalHost();
        }
        if (ip != null) {
            hardwareProp.setHwNetworkClientIPAddress(ip.getHostAddress());
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            hardwareProp.setHwNetworkMACAddress(sb.toString());
        }

    }

    /**
     * Get OS properties using OSHI or JHardware if Windows operating system
     */
    private static void getOSProperties(SystemInfo sysProps) throws Exception {
        LOG.info("Looking for OS properties OSHI...");
        Properties properties = System.getProperties();
        OperatingSystem osSystemProps = sysProps.getOperatingSystem();
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("windows")) {
            OSInfo osInfo = HardwareInfo.getOSInfo();
            hardwareProp.setHwOsName(osInfo.getName());
            Map<String, String> fullOsInfo = osInfo.getFullInfo();
            for (String key : fullOsInfo.keySet()) {
                if ("BuildNumber".equals(key)) {
                    hardwareProp.setHwOsBuildNumber(fullOsInfo.get(key));
                }
            }
        } else {
            hardwareProp.setHwOsName(osSystemProps.getManufacturer() + " " + osSystemProps.getFamily());
            hardwareProp.setHwOsBuildNumber(osSystemProps.getVersionInfo().toString());
        }
        hardwareProp.setHwOsLocalDateTime(properties.getProperty("user.timezone"));
    }

    /* Using JHardware to take some of system environments properties */

    /**
     * Get Memory properties using JHardware fo Windows and linux
     */
    private static void getMemoryInfo() throws Exception {
        LOG.info("Looking for Memory properties JHardware...");
        MemoryInfo infoMemory = HardwareInfo.getMemoryInfo();
        Set<Map.Entry<String, String>> fullInfosMem = infoMemory.getFullInfo().entrySet();
        for (final Map.Entry<String, String> fullInfo : fullInfosMem) {
            switch (fullInfo.getKey()) {
                case "PartNumber":
                    hardwareProp.setHwMemPartNumber(fullInfo.getValue());
                    break;
                case "Manufacturer":
                    hardwareProp.setHwMemManufacturer(fullInfo.getValue());
                    break;
                case "Speed":
                    if (!fullInfo.getValue().equals("")) {
                        hardwareProp.setHwMemSpeed(Double.parseDouble(fullInfo.getValue()));
                    }
                    break;
            }
        }
    }

    /**
     * Get Bios properties using JHardware fo Windows and linux
     */
    private static void setBiosProperties() throws Exception {
        LOG.info("Looking for Bios properties JHardware...");
        BiosInfo infoBios = HardwareInfo.getBiosInfo();
        hardwareProp.setHwBiosVersion(infoBios.getVersion());
        hardwareProp.setHwBiosManufacturer(infoBios.getManufacturer());
        Set<Map.Entry<String, String>> fullInfosBios = infoBios.getFullInfo().entrySet();
        for (final Map.Entry<String, String> fullInfo : fullInfosBios) {
            switch (fullInfo.getKey()) {
                case "Name":
                    hardwareProp.setHwBiosName(fullInfo.getValue());
                    break;
                case "SerialNumber":
                    hardwareProp.setHwBiosSerialNumber(fullInfo.getValue());
                    break;
                case "PrimaryBIOS":
                    hardwareProp.setHwBiosPrimaryBios(Boolean.parseBoolean(fullInfo.getValue()));
                    break;
            }
        }
    }

    /**
     * Get Motherboard porperties using OSHI and JHardware fo Windows and linux
     *
     * @param hwProps
     */
    private static void setMotherboardProperties(HardwareAbstractionLayer hwProps) throws Exception {
        LOG.info("Looking for Motherboard properties JHardware...");
        ComputerSystem comSystemProps = hwProps.getComputerSystem();
        Baseboard baseboardProps = comSystemProps.getBaseboard();
        hardwareProp.setHwMothManufacturer(baseboardProps.getManufacturer());
        MotherboardInfo infoMotherBoard = HardwareInfo.getMotherboardInfo();
        hardwareProp.setHwMothVersion(infoMotherBoard.getVersion());
        Set<Map.Entry<String, String>> fullInfoMotherboard = infoMotherBoard.getFullInfo().entrySet();
        for (final Map.Entry<String, String> fullInfo : fullInfoMotherboard) {
            if (fullInfo.getKey().equals("Product")) {
                hardwareProp.setHwMothProduct(fullInfo.getValue());
            }
        }
    }

    /**
     * Get the Disk type in MAC -OS
     *
     * @param disk
     * @param find
     * @return
     * @throws IOException
     */
    private static String getDiskModelAndType(String disk, String find) throws IOException {
        String[] cmd = {"/bin/sh", "-c", "diskutil info " + disk + " | grep " + find};
        String s;
        String resp = null;
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((s = stdInput.readLine()) != null) {
            resp = s.replace(find.substring(1, find.length() - 1), "").trim();
        }
        return resp;
    }

    /**
     * Fill in the properties available that could not be get because of JHardware
     * not supporting MAC OS
     */
    private static void setPropertiesForMacOS() {
        try {
            SystemInfo sysInfoProps = new SystemInfo();
            HardwareAbstractionLayer hwProps = sysInfoProps.getHardware();

            ComputerSystem systemProps = hwProps.getComputerSystem();
            Firmware firmwareProps = systemProps.getFirmware();
            LOG.info("Looking for EFI properties JHardware...");
            hardwareProp.setHwBiosManufacturer(systemProps.getManufacturer());
            hardwareProp.setHwBiosName(systemProps.getModel());
            hardwareProp.setHwBiosVersion(firmwareProps.getVersion());

            Baseboard baseboardProps = systemProps.getBaseboard();
            LOG.info("Looking for Motherboard properties JHardware...");
            hardwareProp.setHwMothProduct(baseboardProps.getModel());
            hardwareProp.setHwMothVersion(baseboardProps.getVersion());
            hardwareProp.setHwMothManufacturer(baseboardProps.getManufacturer());
        } catch (Exception e) {
            LOG.error(
                    "The hardware properties reading intended for Mac-OS encountered a problem and could not finish successfully");
        }
    }

    /**
     * Check if provided String has any matching strings inside array of strings
     *
     * @param inputStr
     * @param items
     * @return
     */
    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }

    /**
     * Print out all the collected main hardware properties to a file
     */
    public static void outputHardwareDataObjectToFile(Object obj, String fileName) {
        try {
            LOG.info("Writing properties to file...");
            ;
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(obj.toString());
            myWriter.close();
        } catch (IOException e) {
            LOG.error("An error occurred: ", e);
            ;
            e.printStackTrace();
        }
    }
}
