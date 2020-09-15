package com.gocypher.benchmarks.runner.environment.model;

public class JVMProperties {

    private double jvmMemoryInitialMB;
    private double jvmMemoryUsedHeapMB;
    private double jvmMemoryMaxHeapMB;
    private double jvmMemoryCommittedMB;

    private String jvmVmVersion;
    private String jvmVmName;
    private String jvmVmInfo;

    private String jvmRuntimeVersion;
    private String jvmRuntimeName;

    private String jvmVendorURL;
    private String jvmVendor;

    private String jvmUserBootLibraryPath;
    private String jvmUserJavaHome;
    private String jvmUserLanguage;
    private String jvmUserName;

    private String jvmJavaClassVersion;
    private String jvmJavaVersion;
    private String jvmJavaSpecificationName;

    private String jvmOtherManagementCompiler;
    private String jvmOtherEncoding;
    private String jvmOtherIOUnicodeEncoding;

    public double getJvmMemoryInitialMB() {
        return jvmMemoryInitialMB;
    }

    public void setJvmMemoryInitialMB(double jvmMemoryInitialMB) {
        this.jvmMemoryInitialMB = jvmMemoryInitialMB;
    }

    public double getJvmMemoryUsedHeapMB() {
        return jvmMemoryUsedHeapMB;
    }

    public void setJvmMemoryUsedHeapMB(double jvmMemoryUsedHeapMB) {
        this.jvmMemoryUsedHeapMB = jvmMemoryUsedHeapMB;
    }

    public double getJvmMemoryMaxHeapMB() {
        return jvmMemoryMaxHeapMB;
    }

    public void setJvmMemoryMaxHeapMB(double jvmMemoryMaxHeapMB) {
        this.jvmMemoryMaxHeapMB = jvmMemoryMaxHeapMB;
    }

    public double getJvmMemoryCommittedMB() {
        return jvmMemoryCommittedMB;
    }

    public void setJvmMemoryCommittedMB(double jvmMemoryCommittedMB) {
        this.jvmMemoryCommittedMB = jvmMemoryCommittedMB;
    }

    public String getJvmRuntimeName() {
        return jvmRuntimeName;
    }

    public void setJvmRuntimeName(String jvmRuntimeName) {
        this.jvmRuntimeName = jvmRuntimeName;
    }

    public String getJvmUserBootLibraryPath() {
        return jvmUserBootLibraryPath;
    }

    public void setJvmUserBootLibraryPath(String jvmUserBootLibraryPath) {
        this.jvmUserBootLibraryPath = jvmUserBootLibraryPath;
    }

    public String getJvmVmVersion() {
        return jvmVmVersion;
    }

    public void setJvmVmVersion(String jvmVmVersion) {
        this.jvmVmVersion = jvmVmVersion;
    }

    public String getJvmVendorURL() {
        return jvmVendorURL;
    }

    public void setJvmVendorURL(String jvmVendorURL) {
        this.jvmVendorURL = jvmVendorURL;
    }

    public String getJvmVendor() {
        return jvmVendor;
    }

    public void setJvmVendor(String jvmVendor) {
        this.jvmVendor = jvmVendor;
    }

    public String getJvmVmName() {
        return jvmVmName;
    }

    public void setJvmVmName(String jvmVmName) {
        this.jvmVmName = jvmVmName;
    }

    public String getJvmRuntimeVersion() {
        return jvmRuntimeVersion;
    }

    public void setJvmRuntimeVersion(String jvmRuntimeVersion) {
        this.jvmRuntimeVersion = jvmRuntimeVersion;
    }

    public String getJvmUserJavaHome() {
        return jvmUserJavaHome;
    }

    public void setJvmUserJavaHome(String jvmUserJavaHome) {
        this.jvmUserJavaHome = jvmUserJavaHome;
    }

    public String getJvmUserLanguage() {
        return jvmUserLanguage;
    }

    public void setJvmUserLanguage(String jvmUserLanguage) {
        this.jvmUserLanguage = jvmUserLanguage;
    }

    public String getJvmVmInfo() {
        return jvmVmInfo;
    }

    public void setJvmVmInfo(String jvmVmInfo) {
        this.jvmVmInfo = jvmVmInfo;
    }

    public String getJvmOtherIOUnicodeEncoding() {
        return jvmOtherIOUnicodeEncoding;
    }

    public void setJvmOtherIOUnicodeEncoding(String jvmOtherIOUnicodeEncoding) {
        this.jvmOtherIOUnicodeEncoding = jvmOtherIOUnicodeEncoding;
    }

    public String getJvmOtherManagementCompiler() {
        return jvmOtherManagementCompiler;
    }

    public void setJvmOtherManagementCompiler(String jvmOtherManagementCompiler) {
        this.jvmOtherManagementCompiler = jvmOtherManagementCompiler;
    }

    public String getJvmUserName() {
        return jvmUserName;
    }

    public void setJvmUserName(String jvmUserName) {
        this.jvmUserName = jvmUserName;
    }

    public String getJvmJavaClassVersion() {
        return jvmJavaClassVersion;
    }

    public void setJvmJavaClassVersion(String jvmJavaClassVersion) {
        this.jvmJavaClassVersion = jvmJavaClassVersion;
    }

    public String getJvmJavaVersion() {
        return jvmJavaVersion;
    }

    public void setJvmJavaVersion(String jvmJavaVersion) {
        this.jvmJavaVersion = jvmJavaVersion;
    }

    public String getJvmJavaSpecificationName() {
        return jvmJavaSpecificationName;
    }

    public void setJvmJavaSpecificationName(String jvmJavaSpecificationName) {
        this.jvmJavaSpecificationName = jvmJavaSpecificationName;
    }
    public String getJvmOtherEncoding () {
        return jvmOtherEncoding ;
    }

    public void setJvmOtherEncoding (String jvmOtherEncoding ) {
        this.jvmOtherEncoding  = jvmOtherEncoding ;
    }
    @Override
    public String toString() {
        return "JVMProperties{" +System.getProperty("line.separator") +
                    "   jvmMemoryInitialMB : " + jvmMemoryInitialMB + ","+System.getProperty("line.separator") +
                    "   jvmMemoryUsedHeapMB : " + jvmMemoryUsedHeapMB + ","+System.getProperty("line.separator") +
                    "   jvmMemoryMaxHeapMB : " + jvmMemoryMaxHeapMB + ","+System.getProperty("line.separator") +
                    "   jvmMemoryCommittedMB : " + jvmMemoryCommittedMB + ","+System.getProperty("line.separator") +
                    "   jvmUserJavaHome : " + jvmUserJavaHome + ","+System.getProperty("line.separator") +
                    "   jvmUserLanguage : " + jvmUserLanguage + ","+System.getProperty("line.separator") +
                    "   jvmUserName : " + jvmUserName + ","+System.getProperty("line.separator") +
                    "   jvmUserBootLibraryPath : " + jvmUserBootLibraryPath + ","+System.getProperty("line.separator") +
                    "   jvmVmVersion : " + jvmVmVersion + ","+System.getProperty("line.separator") +
                    "   jvmVmInfo : " + jvmVmInfo + ","+System.getProperty("line.separator") +
                    "   jvmVmName : " + jvmVmName + ","+System.getProperty("line.separator") +
                    "   jvmVendorURL : " + jvmVendorURL + ","+System.getProperty("line.separator") +
                    "   jvmVendor : " + jvmVendor + ","+System.getProperty("line.separator") +
                    "   jvmRuntimeName : " + jvmRuntimeName + ","+System.getProperty("line.separator") +
                    "   jvmRuntimeVersion : " + jvmRuntimeVersion + ","+System.getProperty("line.separator") +
                    "   jvmJavaClassVersion : " + jvmJavaClassVersion + ","+System.getProperty("line.separator") +
                    "   jvmJavaVersion : " + jvmJavaVersion + ","+System.getProperty("line.separator") +
                    "   jvmJavaSpecificationName : " + jvmJavaSpecificationName + ","+System.getProperty("line.separator") +
                    "   jvmOtherEncoding  : " + jvmOtherEncoding  + ","+System.getProperty("line.separator") +
                    "   jvmOtherIOUnicodeEncoding : " + jvmOtherIOUnicodeEncoding + System.getProperty("line.separator")  +
                    "   jvmOtherManagementCompiler : " + jvmOtherManagementCompiler + System.getProperty("line.separator")  +
                '}';
    }
}
