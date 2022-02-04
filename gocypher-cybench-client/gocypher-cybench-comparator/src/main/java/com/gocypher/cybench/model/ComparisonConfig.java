/*
 * Copyright (c) 2018-2021, K2N.IO. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * K2N.IO. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with K2N.IO.
 *
 * K2N.IO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. K2N.IO SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */
package com.gocypher.cybench.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComparisonConfig {
    public static enum Scope {
        WITHIN, BETWEEN
    }

    public static enum Method {
        DELTA, SD
    }

    public static enum Threshold {
        PERCENT_CHANGE, GREATER
    }

    private Scope scope;
    private Method method;
    private Threshold threshold;
    private String range;
    private String projectName;
    private String projectVersion;
    private String projectBuild;
    private String compareVersion;
    private List<String> compareBuilds = new ArrayList<>();
    private Double deviationsAllowed;
    private Double percentChangeAllowed;
    private String configName;
    private String configID;

    public ComparisonConfig() {
        compareBuilds = new ArrayList<>();
    }

    public ComparisonConfig(Map<String, Object> configs) {
        setScope((Scope) configs.get("scope"));
        setMethod((Method) configs.get("method"));
        setThreshold((Threshold) configs.get("threshold"));
        setRange((String) configs.get("range"));
        setProjectName((String) configs.get("projectName"));
        setProjectVersion((String) configs.get("projectVersion"));
        setProjectBuild((String) configs.get("projectBuild"));
        setCompareVersion((String) configs.get("compareVersion"));
        setCompareBuilds((List<String>) configs.get("compareBuilds"));
        
        Number deviationsAllowedNum = (Number) configs.get("deviationsAllowed");
        Number percentChangeAllowedNum = (Number) configs.get("percentChangeAllowed");
        if (deviationsAllowedNum != null) {
            setDeviationsAllowed(deviationsAllowedNum.doubleValue());
        }
        if (percentChangeAllowedNum != null) {
            setPercentChangeAllowed(percentChangeAllowedNum.doubleValue());
        }

        setConfigName((String) configs.get("configName"));
        setConfigID((String) configs.get("configID"));
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public void setThreshold(Threshold threshold) {
        this.threshold = threshold;
    }

    public Threshold getThreshold() {
        return threshold;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getRange() {
        return range;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectBuild(String projectBuild) {
        this.projectBuild = projectBuild;
    }

    public String getProjectBuild() {
        return projectBuild;
    }

    public void setCompareVersion(String compareVersion) {
        this.compareVersion = compareVersion;
    }

    public String getCompareVersion() {
        return compareVersion;
    }

    public void setCompareBuilds(List<String> compareBuilds) {
        this.compareBuilds = compareBuilds;
    }

    public List<String> getCompareBuilds() {
        return compareBuilds;
    }

    public void setDeviationsAllowed(Double deviationsAllowed) {
        this.deviationsAllowed = deviationsAllowed;
    }

    public Double getDeviationsAllowed() {
        return deviationsAllowed;
    }

    public void setPercentChangeAllowed(Double percentChangeAllowed) {
        this.percentChangeAllowed = percentChangeAllowed;
    }

    public Double getPercentChangeAllowed() {
        return percentChangeAllowed;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigID(String configID) {
        this.configID = configID;
    }

    public String getConfigID() {
        return configID;
    }
}
