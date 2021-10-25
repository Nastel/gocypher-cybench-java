/*
 * Copyright (C) 2020-2021, K2N.IO.
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

package com.gocypher.cybench.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class ConfigHandling {
    private static final Logger log = LoggerFactory.getLogger(ConfigHandling.class);

    public static final String DEFAULT_COMPARATOR_CONFIG_PATH = "config/comparator.yaml";
    public static final String DEFAULT_REPORTS_LOCATION = "reports/";
    public static final String DEFAULT_TOKEN = "";
    public static final String IDENTIFIER_HEADER = "compare.";
    public static final String DEFAULT_IDENTIFIER_HEADER = "compare.default";
    public static final Comparisons.Method DEFAULT_COMPARE_METHOD = Comparisons.Method.DELTA;
    public static final Comparisons.Scope DEFAULT_COMPARE_SCOPE = Comparisons.Scope.WITHIN;
    public static final Comparisons.Trend DEFAULT_COMPARE_TREND = Comparisons.Trend.NONE;
    public static final Comparisons.Threshold DEFAULT_COMPARE_THRESHOLD = Comparisons.Threshold.GREATER;
    public static final Double DEFAULT_COMPARE_PERCENTAGE = 5.0;
    public static final String DEFAULT_COMPARE_VERSION = null;

    public static Map<String, Object> loadYaml(String... args) {
        File configFile = identifyConfigFile(args);
        Map<String, Object> comparatorProps = null;
        if (configFile.exists()) {
            Yaml configYaml = new Yaml();
            try (Reader rdr = new BufferedReader(new FileReader(configFile))) {
                comparatorProps = configYaml.load(rdr);
            } catch (Exception e) {
                log.error("* Failed to read comparator config, make sure file follows the rules of yaml!", e);
            }
        } else {
            log.warn("No comparator config file found, using default values");
        }
        configHandling(comparatorProps);
        return comparatorProps;
    }

    public static File identifyConfigFile(String... args) {
        String configFilePath = null;
        for (String property : args) {
            if (property.contains("cfg") || property.contains("config")) {
                String[] tempConfigPath = property.split("=");
                if (tempConfigPath.length > 1) {
                    configFilePath = tempConfigPath[1];
                    log.info("Attempting to load comparator configurations at {}\n", configFilePath);
                }
            }
        }
        if (configFilePath == null) {
            configFilePath = DEFAULT_COMPARATOR_CONFIG_PATH;
        }
        File configFile = new File(configFilePath);
        return configFile;
    }

    public static File identifyRecentReport(String reportFolder) {
        File allReports = new File(reportFolder);
        File recentReport = null;
        if (allReports.exists()) {
            File[] reports = allReports.listFiles();
            if (reports != null) {
                for (File report : reports) {
                    String reportPath = report.getAbsolutePath();
                    if (FilenameUtils.getExtension(reportPath).equals("cybench")) {
                        if (recentReport == null || recentReport.lastModified() < report.lastModified()) {
                            recentReport = report;
                        }
                    }
                }
            }
        }
        return recentReport;
    }

    public static void configHandling(Map<String, Object> comparatorProps) {
        if (!comparatorProps.containsKey("reports")) {
            log.warn("Reports key not found in config file - using default reports location: {}",
                    DEFAULT_REPORTS_LOCATION);
            comparatorProps.put("reports", DEFAULT_REPORTS_LOCATION);
        }
        if (!comparatorProps.containsKey("token")) {
            log.warn("Token key not found in config file - will not be able to access private workspaces");
            comparatorProps.put("token", DEFAULT_TOKEN);
        }

        if (!comparatorProps.containsKey(DEFAULT_IDENTIFIER_HEADER)
                || !(comparatorProps.get(DEFAULT_IDENTIFIER_HEADER) instanceof Map<?, ?>)) {
            log.error(
                    "Default compare values not configured properly, check the example comparator.yaml on the Cybench Wiki");
            log.warn("Using predefined defaults (method: {}, scope: {}, trend: {}, threshold: {})",
                    DEFAULT_COMPARE_METHOD, DEFAULT_COMPARE_SCOPE, DEFAULT_COMPARE_TREND, DEFAULT_COMPARE_THRESHOLD);
            Map<String, Object> defaultValues = new HashMap<>();
            defaultValues.put("method", DEFAULT_COMPARE_METHOD);
            defaultValues.put("scope", DEFAULT_COMPARE_SCOPE);
            defaultValues.put("trend", DEFAULT_COMPARE_TREND);
            defaultValues.put("threshold", DEFAULT_COMPARE_THRESHOLD);
            comparatorProps.put(DEFAULT_IDENTIFIER_HEADER, defaultValues);
        } else {
            checkConfigValidity(DEFAULT_IDENTIFIER_HEADER, comparatorProps);
        }
    }

    @SuppressWarnings("unchecked")
    private static String checkConfigValidity(String identifier, Map<String, Object> comparatorProps) {
        String simplifiedIdentifier = identifier.split(IDENTIFIER_HEADER)[1];
        String packageName = null;
        if (comparatorProps.get(identifier) instanceof Map<?, ?>) {
            Map<String, Object> compareVals = (HashMap<String, Object>) comparatorProps.get(identifier);
            Map<String, Object> defaultVals = (HashMap<String, Object>) comparatorProps.get(DEFAULT_IDENTIFIER_HEADER);

            Comparisons.Method defaultMethod = DEFAULT_COMPARE_METHOD;
            Comparisons.Scope defaultScope = DEFAULT_COMPARE_SCOPE;
            Comparisons.Trend defaultTrend = DEFAULT_COMPARE_TREND;
            Comparisons.Threshold defaultThreshold = DEFAULT_COMPARE_THRESHOLD;
            Double defaultPercentage = DEFAULT_COMPARE_PERCENTAGE;
            String defaultVersion = DEFAULT_COMPARE_VERSION;

            if (!simplifiedIdentifier.equals("default")) {
                if (defaultVals.containsKey("method")) {
                    defaultMethod = (Comparisons.Method) defaultVals.get("method");
                }
                if (defaultVals.containsKey("scope")) {
                    defaultScope = (Comparisons.Scope) defaultVals.get("scope");
                }
                if (defaultVals.containsKey("trend")) {
                    defaultTrend = (Comparisons.Trend) defaultVals.get("trend");
                }
                if (defaultVals.containsKey("threshold")) {
                    defaultThreshold = (Comparisons.Threshold) defaultVals.get("threshold");
                }
                if (defaultVals.containsKey("percentage")) {
                    defaultPercentage = (Double) defaultVals.get("percentage");
                }
                if (defaultVals.containsKey("version")) {
                    defaultVersion = (String) defaultVals.get("version");
                }
            }

            if (!compareVals.containsKey("method")) {
                log.warn("'{}': Method not defined, will use: {}", simplifiedIdentifier, defaultMethod);
                compareVals.put("method", defaultMethod);
            } else {
                String method = (String) compareVals.get("method");
                method = method.toUpperCase();
                if (!EnumUtils.isValidEnum(Comparisons.Method.class, method)) {
                    log.warn("'{}': '{}' found in config file is not a valid comparison method - will use: {}",
                            simplifiedIdentifier, method, defaultMethod);
                    method = defaultMethod.toString();
                }
                Comparisons.Method methodEnum = Comparisons.Method.valueOf(method);
                compareVals.put("method", methodEnum);
            }

            if (!compareVals.containsKey("trend")) {
                log.warn("'{}': Trend not defined, will use: {}", simplifiedIdentifier, defaultTrend);
                compareVals.put("trend", defaultTrend);
            } else {
                String trend = (String) compareVals.get("trend");
                trend = trend.toUpperCase();
                if (!EnumUtils.isValidEnum(Comparisons.Trend.class, trend)) {
                    log.warn("'{}': '{}' found in config file is not a valid comparison trend - will use: {}",
                            simplifiedIdentifier, trend, defaultTrend);
                    trend = defaultTrend.toString();
                }
                Comparisons.Trend trendEnum = Comparisons.Trend.valueOf(trend);
                compareVals.put("trend", trendEnum);
            }

            if (!compareVals.containsKey("scope")) {
                log.warn("'{}': Scope not defined, will use: {}", simplifiedIdentifier, defaultScope);
                compareVals.put("scope", defaultScope);
            } else {
                String scope = (String) compareVals.get("scope");
                scope = scope.toUpperCase();
                if (!EnumUtils.isValidEnum(Comparisons.Scope.class, scope)) {
                    log.warn("'{}': '{}' found in config file is not a valid comparison scope - will use: {}",
                            simplifiedIdentifier, scope, defaultScope);
                    scope = defaultScope.toString();
                }
                Comparisons.Scope scopeEnum = Comparisons.Scope.valueOf(scope);
                compareVals.put("scope", scopeEnum);
            }

            Comparisons.Scope compareScope = (Comparisons.Scope) compareVals.get("scope");
            if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
                if (!compareVals.containsKey("version")) {
                    if (defaultVals.containsKey("version")) {
                        compareVals.put("version", defaultVersion);
                        log.warn("'{}': Version not specified, will compare to version: {}", simplifiedIdentifier,
                                defaultVersion);
                    } else {
                        log.warn(
                                "'{}': Compare scope recognized as 'BETWEEN' but no compare version was specified - please provide a version to compare to",
                                simplifiedIdentifier);
                        log.warn("'{}': Will use scope: {}", simplifiedIdentifier, defaultScope);
                        compareVals.put("scope", defaultScope);
                        compareVals.remove("version");
                    }
                } else {
                    String version = (String) compareVals.get("version");
                    compareVals.put("version", version);
                    log.info("'{}': Will compare to version: {}", simplifiedIdentifier, version);
                }
            } else {
                if (compareVals.containsKey("version") && compareScope.equals(Comparisons.Scope.WITHIN)) {
                    log.warn(
                            "'{}': Version was specified, but scope was specified as 'WITHIN' - will compare within benchmarked version",
                            simplifiedIdentifier);
                    compareVals.remove("version");
                } else if (compareVals.containsKey("version")) {
                    String version = (String) compareVals.get("version");
                    if (defaultScope.equals(Comparisons.Scope.BETWEEN)) {
                        compareVals.put("version", version);
                        log.info("'{}': Will compare to version: {}", simplifiedIdentifier, version);
                    }
                }
            }

            if (!compareVals.containsKey("threshold")) {
                log.warn("'{}': Threshold not defined, will use: {}", simplifiedIdentifier, defaultThreshold);
                compareVals.put("threshold", defaultThreshold);
            } else {
                String threshold = (String) compareVals.get("threshold");
                threshold = threshold.toUpperCase();
                if (!EnumUtils.isValidEnum(Comparisons.Threshold.class, threshold)) {
                    log.warn("'{}': '{}' found in config file is not a valid comparison threshold - will use: {}",
                            simplifiedIdentifier, threshold, defaultThreshold);
                    threshold = defaultThreshold.toString();
                }
                Comparisons.Threshold thresholdEnum = Comparisons.Threshold.valueOf(threshold);
                compareVals.put("threshold", thresholdEnum);
            }

            Comparisons.Threshold compareThreshold = (Comparisons.Threshold) compareVals.get("threshold");
            if (compareThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                if (!compareVals.containsKey("percentage")) {
                    if (defaultVals.containsKey("percentage")) {
                        compareVals.put("percentage", defaultPercentage);
                        log.warn("'{}': Percentage not specified, will use percentage: {}", simplifiedIdentifier,
                                defaultPercentage);
                    } else {
                        log.warn(
                                "'{}': Compare threshold recognized as 'PERCENT_CHANGE' but no compare percentage was specified - please provide a percentage to compare with",
                                simplifiedIdentifier);
                        log.warn("'{}': Will use threshold: {}", simplifiedIdentifier, defaultThreshold);
                        compareVals.put("threshold", defaultThreshold);
                        compareVals.remove("percentage");
                    }
                } else {
                    String percentageStr = (String) compareVals.get("percentage");
                    try {
                        Double percentage = Double.parseDouble(percentageStr);
                        compareVals.put("percentage", percentage);
                        log.info("'{}': Will compare with percentage: {}", simplifiedIdentifier, percentage);
                    } catch (Exception e) {
                        log.warn("'{}': '{}' found in config file is not a valid number - will use: {}",
                                simplifiedIdentifier, percentageStr, defaultPercentage);
                        compareVals.put("percentage", defaultPercentage);
                    }
                }
            } else {
                if (compareVals.containsKey("percentage") && compareThreshold.equals(Comparisons.Threshold.GREATER)) {
                    log.warn(
                            "'{}': Percentage was specified, but threshold was specified as 'GREATER' - will compare using the 'GREATER' threshold and ignore percentage value",
                            simplifiedIdentifier);
                    compareVals.remove("percentage");
                } else if (compareVals.containsKey("percentage")) {
                    String percentageStr = (String) compareVals.get("percentage");
                    try {
                        Double percentage = Double.parseDouble(percentageStr);
                        if (defaultThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                            compareVals.put("percentage", percentage);
                            log.info("'{}': Will compare with percentage: {}", simplifiedIdentifier, percentage);
                        }
                    } catch (Exception e) {
                        log.warn("'{}': '{}' found in config file is not a valid number - will use: {}",
                                simplifiedIdentifier, percentageStr, defaultPercentage);
                        compareVals.put("percentage", defaultPercentage);
                    }

                }
            }

            comparatorProps.put(identifier, compareVals);
            if (compareVals.containsKey("package")) {
                packageName = (String) compareVals.get("package");
            } else if (simplifiedIdentifier.equals("default")) {
                packageName = "default";
            } else {
                log.warn(
                        "'{}': NO PACKAGE SPECIFIED for comparisons! Won't use these configurations - please specify a package",
                        simplifiedIdentifier);
            }

            log.info("{}={}\n", packageName, compareVals);
        } else {
            log.error("'{}' is not defined properly! Check the example comparator.yaml on the Cybench Wiki",
                    simplifiedIdentifier);
        }
        return packageName;
    }

    public static Map<String, String> identifyAndValidifySpecificConfigs(Map<String, Object> comparatorProps) {
        Map<String, String> specificIdentifiers = new HashMap<>();
        for (String identifier : comparatorProps.keySet()) {
            if (identifier.contains(IDENTIFIER_HEADER) && !identifier.equals(DEFAULT_IDENTIFIER_HEADER)) {
                String packageName = checkConfigValidity(identifier, comparatorProps);
                specificIdentifiers.put(packageName, identifier);
            }
        }
        return specificIdentifiers;
    }
}
