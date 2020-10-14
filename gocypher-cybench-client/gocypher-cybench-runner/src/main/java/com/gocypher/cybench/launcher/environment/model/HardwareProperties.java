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

package com.gocypher.cybench.launcher.environment.model;

public class HardwareProperties {

    /**
     * hw stand for hardware information.
     * proc - stand for processor information.
     * mem - stand for memory information.
     * bios - stand for bios information.
     * moth - stand for motherboard information.
     * os - stand for Operating System information.
     * graphicC - stand for graphic card information.
     * hardD - hard drive memory information.
     */
    private String hwProcName;
    private String hwProcSpeed;
    private String hwProcManufacturer;
    private String hwProcMicroArchitecture;
    private Integer hwProcCoresCount;
    private Integer hwProcLogicalThreadsCount;

    private String hwMemPartNumber;
    private Double hwMemTotalMemoryBytes;
    private Double hwMemSpeed;
    private Double hwMemAvailableMemoryBytes;
    private String hwMemManufacturer;

    private Boolean hwBiosPrimaryBios;
    private String hwBiosManufacturer;
    private String hwBiosSerialNumber;
    private String hwBiosName;
    private String hwBiosVersion;

    private String hwMothManufacturer;
    private String hwMothVersion;
    private String hwMothProduct;

    private String hwOsName;
    private String hwOsLocalDateTime;
    private String hwOsBuildNumber;

    private String hwGraphicCName;

    private String hwLaunchDiskModel;
    private String hwLaunchDiskType;
    private Double hwLaunchDiskTotalMemoryGB;
    private Double hwLaunchDiskFreeMemoryGB;

    private String hwNetworkMACAddress;
    private String hwNetworkClientIPAddress;


    public String getHwProcName() {
        return hwProcName;
    }

    public void setHwProcName(String hwProcName) {
        this.hwProcName = hwProcName;
    }

    public String getHwProcSpeed() {
        return hwProcSpeed;
    }

    public void setHwProcSpeed(String hwProcSpeed) {
        this.hwProcSpeed = hwProcSpeed;
    }

    public String getHwProcManufacturer() {
        return hwProcManufacturer;
    }

    public void setHwProcManufacturer(String hwProcManufacturer) {
        this.hwProcManufacturer = hwProcManufacturer;
    }

    public Integer getHwProcCoresCount() {
        return hwProcCoresCount;
    }

    public void setHwProcCoresCount(Integer hwProcCoresCount) {
        this.hwProcCoresCount = hwProcCoresCount;
    }

    public Integer getHwProcLogicalThreadsCount() {
        return hwProcLogicalThreadsCount;
    }

    public void setHwProcLogicalThreadsCount(Integer hwProcLogicalThreadsCount) {
        this.hwProcLogicalThreadsCount = hwProcLogicalThreadsCount;
    }

    public String getHwMemPartNumber() {
        return hwMemPartNumber;
    }

    public void setHwMemPartNumber(String hwMemPartNumber) {
        this.hwMemPartNumber = hwMemPartNumber;
    }


    public Double getHwMemAvailableMemoryBytes() {
        return hwMemAvailableMemoryBytes;
    }

    public void setHwMemAvailableMemoryBytes(Double hwMemAvailableMemoryBytes) {
        this.hwMemAvailableMemoryBytes = hwMemAvailableMemoryBytes;
    }

    public String getHwBiosManufacturer() {
        return hwBiosManufacturer;
    }

    public void setHwBiosManufacturer(String hwBiosManufacturer) {
        this.hwBiosManufacturer = hwBiosManufacturer;
    }

    public String getHwBiosSerialNumber() {
        return hwBiosSerialNumber;
    }

    public void setHwBiosSerialNumber(String hwBiosSerialNumber) {
        this.hwBiosSerialNumber = hwBiosSerialNumber;
    }

    public String getHwBiosName() {
        return hwBiosName;
    }

    public void setHwBiosName(String hwBiosName) {
        this.hwBiosName = hwBiosName;
    }

    public String getHwBiosVersion() {
        return hwBiosVersion;
    }

    public void setHwBiosVersion(String hwBiosVersion) {
        this.hwBiosVersion = hwBiosVersion;
    }

    public String getHwMothVersion() {
        return hwMothVersion;
    }

    public void setHwMothVersion(String hwMothVersion) {
        this.hwMothVersion = hwMothVersion;
    }

    public String getHwMothProduct() {
        return hwMothProduct;
    }

    public void setHwMothProduct(String hwMothProduct) {
        this.hwMothProduct = hwMothProduct;
    }

    public String getHwOsName() {
        return hwOsName;
    }

    public void setHwOsName(String hwOsName) {
        this.hwOsName = hwOsName;
    }

    public String getHwOsLocalDateTime() {
        return hwOsLocalDateTime;
    }

    public void setHwOsLocalDateTime(String hwOsLocalDateTime) {
        this.hwOsLocalDateTime = hwOsLocalDateTime;
    }

    public String getHwOsBuildNumber() {
        return hwOsBuildNumber;
    }

    public void setHwOsBuildNumber(String hwOsBuildNumber) {
        this.hwOsBuildNumber = hwOsBuildNumber;
    }

    public String getHwGraphicCName() {
        return hwGraphicCName;
    }

    public void setHwGraphicCName(String hwGraphicCName) {
        this.hwGraphicCName = hwGraphicCName;
    }

    public String getHwNetworkMACAddress() {
        return hwNetworkMACAddress;
    }

    public void setHwNetworkMACAddress(String hwNetworkMACAddress) {
        this.hwNetworkMACAddress = hwNetworkMACAddress;
    }

    private String formatString(Double doubleValue, Integer floatingPoInteger){
        return String.format ("%."+floatingPoInteger+"f", doubleValue);
    }

    public Double getHwMemSpeed() {
        return hwMemSpeed;
    }

    public void setHwMemSpeed(Double hwMemSpeed) {
        this.hwMemSpeed = hwMemSpeed;
    }

    public String getHwProcMicroArchitecture() {
        return hwProcMicroArchitecture;
    }

    public void setHwProcMicroArchitecture(String hwProcMicroArchitecture) {
        this.hwProcMicroArchitecture = hwProcMicroArchitecture;
    }

    public Double getHwMemTotalMemoryBytes() {
        return hwMemTotalMemoryBytes;
    }

    public void setHwMemTotalMemoryBytes(Double hwMemTotalMemoryBytes) {
        this.hwMemTotalMemoryBytes = hwMemTotalMemoryBytes;
    }

    public void setHwLaunchDiskModel(String hwLaunchDiskModel) {
        this.hwLaunchDiskModel = hwLaunchDiskModel;
    }

    public String getHwLaunchDiskModel() {
        return hwLaunchDiskModel;
    }

    public Double getHwLaunchDiskTotalMemoryGB() {
        return hwLaunchDiskTotalMemoryGB;
    }

    public void setHwLaunchDiskTotalMemoryGB(Double hwLaunchDiskTotalMemoryGB) {
        this.hwLaunchDiskTotalMemoryGB = hwLaunchDiskTotalMemoryGB;
    }
    public String getHwMothManufacturer() {
        return hwMothManufacturer;
    }

    public void setHwMothManufacturer(String hwMothManufacturer) {
        this.hwMothManufacturer = hwMothManufacturer;
    }
    public Double getHwLaunchDiskFreeMemoryGB() {
        return hwLaunchDiskFreeMemoryGB;
    }

    public void setHwLaunchDiskFreeMemoryGB(Double hwLaunchDiskFreeMemoryGB) {
        this.hwLaunchDiskFreeMemoryGB = hwLaunchDiskFreeMemoryGB;
    }

    public String getHwMemManufacturer() {
        return hwMemManufacturer;
    }

    public void setHwMemManufacturer(String hwMemManufacturer) {
        this.hwMemManufacturer = hwMemManufacturer;
    }

    public Boolean isHwBiosPrimaryBios() {
        return hwBiosPrimaryBios;
    }

    public void setHwBiosPrimaryBios(Boolean hwBiosPrimaryBios) {
        this.hwBiosPrimaryBios = hwBiosPrimaryBios;
    }

    public String getHwLaunchDiskType() {
        return hwLaunchDiskType;
    }

    public void setHwLaunchDiskType(String hwLaunchDiskType) {
        this.hwLaunchDiskType = hwLaunchDiskType;
    }

    public Boolean getHwBiosPrimaryBios() {
        return hwBiosPrimaryBios;
    }

    public String getHwNetworkClientIPAddress() {
        return hwNetworkClientIPAddress;
    }

    public void setHwNetworkClientIPAddress(String hwNetworkClientIPAddress) {
        this.hwNetworkClientIPAddress = hwNetworkClientIPAddress;
    }

    @Override
    public String toString() {
        return "HardwareProperties{" + System.getProperty("line.separator") +
                " hwProcName : " + hwProcName + "," + System.getProperty("line.separator") +
                " hwProcManufacturer : " + hwProcManufacturer + "," + System.getProperty("line.separator") +
                " hwProcMicroArchitecture : " + hwProcMicroArchitecture + "," + System.getProperty("line.separator") +
                " hwProcSpeed : " + hwProcSpeed + "," + System.getProperty("line.separator") +
                " hwProcCoresCount : " + hwProcCoresCount + "," + System.getProperty("line.separator") +
                " hwProcLogicalThreadsCount : " + hwProcLogicalThreadsCount + "," + System.getProperty("line.separator") +

                " hwMemPartNumber : " + hwMemPartNumber + "," + System.getProperty("line.separator") +
                " hwMemSpeed : " + formatString(hwMemSpeed,0) + "," + System.getProperty("line.separator") +
                " hwMemTotalMemoryBytes : " + formatString(hwMemTotalMemoryBytes,0) + "," + System.getProperty("line.separator") +
                " hwMemAvailableMemoryBytes : " + formatString(hwMemAvailableMemoryBytes,0) + "," + System.getProperty("line.separator") +
                " hwMemManufacturer : " + hwMemManufacturer + "," + System.getProperty("line.separator") +

                " hwBiosPrimaryBios : " + hwBiosPrimaryBios + "," + System.getProperty("line.separator") +
                " hwBiosManufacturer : " + hwBiosManufacturer + "," + System.getProperty("line.separator") +
                " hwBiosSerialNumber : " + hwBiosSerialNumber + "," + System.getProperty("line.separator") +
                " hwBiosName : " + hwBiosName + "," + System.getProperty("line.separator") +
                " hwBiosVersion : " + hwBiosVersion + "," + System.getProperty("line.separator") +

                " hwMothVersion : " + hwMothVersion + "," + System.getProperty("line.separator") +
                " hwMothProduct : " + hwMothProduct + "," + System.getProperty("line.separator") +
                " hwMothManufacturer : " + hwMothManufacturer + "," + System.getProperty("line.separator") +

                " hwOsName : " + hwOsName + "," + System.getProperty("line.separator") +
                " hwOsLocalDateTime : " + hwOsLocalDateTime + "," + System.getProperty("line.separator") +
                " hwOsBuildNumber : " + hwOsBuildNumber + "," + System.getProperty("line.separator") +

                " hwGraphicCName : " + hwGraphicCName + "," + System.getProperty("line.separator") +

                " hwLaunchDiskModel : " + hwLaunchDiskModel + "," + System.getProperty("line.separator") +
                " hwLaunchDiskType : " + hwLaunchDiskType + "," + System.getProperty("line.separator") +
                " hwLaunchDiskTotalMemoryGB : " + formatString(hwLaunchDiskTotalMemoryGB,4) + "," + System.getProperty("line.separator") +
                " hwLaunchDiskFreeMemoryGB : " + formatString(hwLaunchDiskFreeMemoryGB,4) + "," + System.getProperty("line.separator") +

                " hwNetworkMACAddress : " + hwNetworkMACAddress + System.getProperty("line.separator") +
                " hwNetworkClientIPAddress : " + hwNetworkClientIPAddress + System.getProperty("line.separator") +
                '}';
    }

}