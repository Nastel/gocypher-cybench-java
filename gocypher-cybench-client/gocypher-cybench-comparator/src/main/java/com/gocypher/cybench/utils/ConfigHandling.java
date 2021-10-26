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
    public static final String DEFAULT_COMPARE_RANGE = "1";
    public static final Comparisons.Threshold DEFAULT_COMPARE_THRESHOLD = Comparisons.Threshold.GREATER;
    public static final Double DEFAULT_DEVIATIONS_ALLOWED = 1.0;
    public static final Double DEFAULT_PERCENTAGE_ALLOWED = 5.0;
    public static final String DEFAULT_COMPARE_VERSION = null;

    public static Map<String, Object> loadYaml(String configFilePath) {
        File configFile = identifyConfigFile(configFilePath);
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

    public static File identifyConfigFile(String configFilePath) {
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
            log.warn("Using predefined defaults (method: {}, scope: {}, range: {}, threshold: {})",
                    DEFAULT_COMPARE_METHOD, DEFAULT_COMPARE_SCOPE, DEFAULT_COMPARE_RANGE, DEFAULT_COMPARE_THRESHOLD);
            Map<String, Object> defaultValues = new HashMap<>();
            defaultValues.put("method", DEFAULT_COMPARE_METHOD);
            defaultValues.put("scope", DEFAULT_COMPARE_SCOPE);
            defaultValues.put("range", DEFAULT_COMPARE_RANGE);
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
            String defaultRange = DEFAULT_COMPARE_RANGE;
            Comparisons.Threshold defaultThreshold = DEFAULT_COMPARE_THRESHOLD;
            Double defaultPercentage = DEFAULT_PERCENTAGE_ALLOWED;
            Double defaultDeviations = DEFAULT_DEVIATIONS_ALLOWED;
            String defaultVersion = DEFAULT_COMPARE_VERSION;

            if (!simplifiedIdentifier.equals("default")) {
                if (defaultVals.containsKey("method")) {
                    defaultMethod = (Comparisons.Method) defaultVals.get("method");
                }
                if (defaultVals.containsKey("scope")) {
                    defaultScope = (Comparisons.Scope) defaultVals.get("scope");
                }
                if (defaultVals.containsKey("range")) {
                    defaultRange = (String) defaultVals.get("range");
                }
                if (defaultVals.containsKey("threshold")) {
                    defaultThreshold = (Comparisons.Threshold) defaultVals.get("threshold");
                }
                if (defaultVals.containsKey("percentageAllowed")) {
                    defaultPercentage = (Double) defaultVals.get("percentageAllowed");
                }
                if (defaultVals.containsKey("deviationsAllowed")) {
                    defaultDeviations = (Double) defaultVals.get("deviationsAllowed");
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

            Comparisons.Method compareMethod = (Comparisons.Method) compareVals.get("method");
            if (compareMethod.equals(Comparisons.Method.SD)) {
                if (!compareVals.containsKey("deviationsAllowed")) {
                    compareVals.put("deviationsAllowed", defaultDeviations);
                    log.warn("'{}': Deviations Allowed not specified, will allow: {}", simplifiedIdentifier,
                            defaultDeviations);
                } else {
                    String deviationsStr = (String) compareVals.get("deviationsAllowed");
                    try {
                        Double deviations = Double.parseDouble(deviationsStr);
                        compareVals.put("deviationsAllowed", deviationsStr);
                        log.info("'{}': Will compare allowing deviations: {}", simplifiedIdentifier, deviations);
                    } catch (Exception e) {
                        log.warn("'{}': '{}' found in config file is not a valid number - will use: {}",
                                simplifiedIdentifier, deviationsStr, defaultDeviations);
                        compareVals.put("deviationsAllowed", defaultDeviations);
                    }
                }
            } else {
                if (compareVals.containsKey("deviationsAllowed")) {
                    log.warn(
                            "'{}': Deviations allowed were specified, but method was specified as 'DELTA' - comparison does not require deviation values",
                            simplifiedIdentifier);
                    compareVals.remove("deviationsAllowed");
                }
            }

            if (!compareVals.containsKey("range")) {
                log.warn("'{}': Range not defined, will use: {}", simplifiedIdentifier, defaultRange);
                compareVals.put("range", defaultRange);
            } else {
                String range = (String) compareVals.get("range");
                range = range.toUpperCase();
                if (!range.equals("ALL")) {
                    try {
                        Integer rangeInt = Integer.parseInt(range);
                    } catch (Exception e) {
                        log.warn("'{}': '{}' found in config file is not a valid comparison range - will use: {}",
                                simplifiedIdentifier, range, defaultRange);
                        range = defaultRange;
                    }
                }
                compareVals.put("range", range);
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

            if (compareMethod.equals(Comparisons.Method.DELTA)) {
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
                    if (!compareVals.containsKey("percentageAllowed")) {
                        if (defaultVals.containsKey("percentageAllowed")) {
                            compareVals.put("percentageAllowed", defaultPercentage);
                            log.warn("'{}': Percentage not specified, will use percentage: {}", simplifiedIdentifier,
                                    defaultPercentage);
                        } else {
                            log.warn(
                                    "'{}': Compare threshold recognized as 'PERCENT_CHANGE' but no compare percentage was specified - please provide a percentage to compare with",
                                    simplifiedIdentifier);
                            log.warn("'{}': Will use threshold: {}", simplifiedIdentifier, defaultThreshold);
                            compareVals.put("threshold", defaultThreshold);
                            compareVals.remove("percentageAllowed");
                        }
                    } else {
                        String percentageStr = (String) compareVals.get("percentageAllowed");
                        try {
                            Double percentage = Double.parseDouble(percentageStr);
                            compareVals.put("percentageAllowed", percentage);
                            log.info("'{}': Will compare with percentage: {}", simplifiedIdentifier, percentage);
                        } catch (Exception e) {
                            log.warn("'{}': '{}' found in config file is not a valid number - will use: {}",
                                    simplifiedIdentifier, percentageStr, defaultPercentage);
                            compareVals.put("percentageAllowed", defaultPercentage);
                        }
                    }
                } else {
                    if (compareVals.containsKey("percentageAllowed")
                            && compareThreshold.equals(Comparisons.Threshold.GREATER)) {
                        log.warn(
                                "'{}': Percentage was specified, but threshold was specified as 'GREATER' - will compare using the 'GREATER' threshold and ignore percentage value",
                                simplifiedIdentifier);
                        compareVals.remove("percentageAllowed");
                    } else if (compareVals.containsKey("percentageAllowed")) {
                        String percentageStr = (String) compareVals.get("percentageAllowed");
                        try {
                            Double percentage = Double.parseDouble(percentageStr);
                            if (defaultThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                                compareVals.put("percentageAllowed", percentage);
                                log.info("'{}': Will compare with percentage: {}", simplifiedIdentifier, percentage);
                            }
                        } catch (Exception e) {
                            log.warn("'{}': '{}' found in config file is not a valid number - will use: {}",
                                    simplifiedIdentifier, percentageStr, defaultPercentage);
                            compareVals.put("percentageAllowed", defaultPercentage);
                        }

                    }
                }
            } else {
                if (compareVals.containsKey("percentageAllowed")) {
                    log.warn(
                            "'{}': Percentage was specified but method was specified as 'SD' - will ignore percentage and only compare using deviations allowed",
                            simplifiedIdentifier);
                    compareVals.remove("percentageAllowed");
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