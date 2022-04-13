/*
 * Copyright (C) 2020-2022, K2N.IO.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
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

    public static enum Type {
        SD, DELTA, PERCENT_CHANGE
    }

    private Type testType;
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
    private Integer anomaliesAllowed;
    private Integer compareLatestReports;
    private boolean shouldRunComparison = true;

    public ComparisonConfig() {
        compareBuilds = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public ComparisonConfig(Map<String, Object> configs) {
        String scope = String.valueOf(configs.get("scope"));
        String method = String.valueOf(configs.get("method"));
        setScope(Scope.valueOf(scope));
        setMethod(Method.valueOf(method));
        if (configs.containsKey("threshold") && configs.get("threshold") != null) {
            String threshold = String.valueOf(configs.get("threshold"));
            setThreshold(Threshold.valueOf(threshold));
        }

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

    public void setTestType() {
        if (method == Method.SD) {
            setTestType(Type.SD);
        } else if (threshold == Threshold.GREATER) {
            setTestType(Type.DELTA);
        } else if (threshold == Threshold.PERCENT_CHANGE) {
            setTestType(Type.PERCENT_CHANGE);
        }
    }

    public void setTestType(Type testType) {
        this.testType = testType;
    }

    public Type getTestType() {
        return testType;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public void setMethod(Method method) {
        this.method = method;
        setTestType();
    }

    public Method getMethod() {
        return method;
    }

    public void setThreshold(Threshold threshold) {
        this.threshold = threshold;
        setTestType();
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

    public void setAnomaliesAllowed(Integer anomaliesAllowed) {
        this.anomaliesAllowed = anomaliesAllowed;
    }

    public Integer getAnomaliesAllowed() {
        return anomaliesAllowed;
    }

    public void setCompareLatestReports(Integer compareLatestReports) {
        this.compareLatestReports = compareLatestReports;
    }

    public Integer getCompareLatestReports() {
        return compareLatestReports;
    }

    public void setShouldRunComparison(boolean shouldRunComparison) {
        this.shouldRunComparison = shouldRunComparison;
    }

    public boolean getShouldRunComparison() {
        return shouldRunComparison;
    }

    @Override
    public String toString() {
        return "Comparison Config - type: " + testType //
                + ", scope: " + scope //
                + ", method: " + method //
                + ", threshold: " + threshold //
                + ", range: " + range //
                + ", projectName: " + projectName //
                + ", projectVersion: " + projectVersion //
                + ", projectBuild: " + projectBuild //
                + ", compareVersion: " + compareVersion //
                + ", compareBuilds: " + compareBuilds //
                + ", deviationsAllowed: " + deviationsAllowed //
                + ", percentChangeAllowed: " + percentChangeAllowed //
                + ", configName: " + configName //
                + ", configID: " + configID //
                + ", shouldRunComparison: " + shouldRunComparison; //
    }
}
