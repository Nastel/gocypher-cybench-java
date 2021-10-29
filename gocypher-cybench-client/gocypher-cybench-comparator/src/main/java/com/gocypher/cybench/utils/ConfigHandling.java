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

    // DEFAULT VALUES
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

    // FINAL STRING KEYS
    public static final String TOKEN = "token";
    public static final String REPORT_PATH = "reportPath";
    public static final String CONFIG_PATH = "configPath";
    public static final String SCRIPT_PATH = "scriptPath";
    public static final String METHOD = "method";
    public static final String SCOPE = "scope";
    public static final String RANGE = "range";
    public static final String THRESHOLD = "threshold";
    public static final String COMPARE_VERSION = "compareVersion";
    public static final String PERCENT_CHANGE_ALLOWED = "percentChangeAllowed";
    public static final String DEVIATIONS_ALLOWED = "deviationsAllowed";

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
            log.warn("Reports key not passed - using default reports location: {}", DEFAULT_REPORTS_LOCATION);
            comparatorProps.put("reports", DEFAULT_REPORTS_LOCATION);
        }
        if (!comparatorProps.containsKey(TOKEN)) {
            log.warn("Token key not passed - will not be able to access private workspaces");
            comparatorProps.put(TOKEN, DEFAULT_TOKEN);
        }

        if (!comparatorProps.containsKey(DEFAULT_IDENTIFIER_HEADER)
                || !(comparatorProps.get(DEFAULT_IDENTIFIER_HEADER) instanceof Map<?, ?>)) {
            log.error(
                    "Default compare values not configured properly, check the example comparator.yaml on the Cybench Wiki");
            log.warn("Using predefined defaults (method: {}, scope: {}, range: {}, threshold: {})",
                    DEFAULT_COMPARE_METHOD, DEFAULT_COMPARE_SCOPE, DEFAULT_COMPARE_RANGE, DEFAULT_COMPARE_THRESHOLD);

            comparatorProps.put(DEFAULT_IDENTIFIER_HEADER, loadDefaults());
        } else {
            checkConfigValidity(DEFAULT_IDENTIFIER_HEADER, comparatorProps);
        }
    }

    public static Map<String, Object> loadDefaults() {
        Map<String, Object> defaultValues = new HashMap<>();
        defaultValues.put(METHOD, DEFAULT_COMPARE_METHOD);
        defaultValues.put(SCOPE, DEFAULT_COMPARE_SCOPE);
        defaultValues.put(RANGE, DEFAULT_COMPARE_RANGE);
        defaultValues.put(THRESHOLD, DEFAULT_COMPARE_THRESHOLD);
        return defaultValues;
    }

    @SuppressWarnings("unchecked")
    protected static String checkConfigValidity(String identifier, Map<String, Object> comparatorProps) {
        String simplifiedIdentifier = identifier;
        try {
            if (!identifier.equals("MyScript")) {
                simplifiedIdentifier = identifier.split(IDENTIFIER_HEADER)[1];
            }
        } catch (Exception e) {
            log.error("{} not defined correctly! Identifier needs to start with 'compare.'", identifier);
        }
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
                if (defaultVals.containsKey(METHOD)) {
                    defaultMethod = (Comparisons.Method) defaultVals.get(METHOD);
                }
                if (defaultVals.containsKey(SCOPE)) {
                    defaultScope = (Comparisons.Scope) defaultVals.get(SCOPE);
                }
                if (defaultVals.containsKey(RANGE)) {
                    defaultRange = (String) defaultVals.get(RANGE);
                }
                if (defaultVals.containsKey(THRESHOLD)) {
                    defaultThreshold = (Comparisons.Threshold) defaultVals.get(THRESHOLD);
                }
                if (defaultVals.containsKey(PERCENT_CHANGE_ALLOWED)) {
                    defaultPercentage = (Double) defaultVals.get(PERCENT_CHANGE_ALLOWED);
                }
                if (defaultVals.containsKey(DEVIATIONS_ALLOWED)) {
                    defaultDeviations = (Double) defaultVals.get(DEVIATIONS_ALLOWED);
                }
                if (defaultVals.containsKey(COMPARE_VERSION)) {
                    defaultVersion = (String) defaultVals.get(COMPARE_VERSION);
                }
            }

            if (!compareVals.containsKey(METHOD)) {
                log.warn("'{}': Method not defined, will use: {}", simplifiedIdentifier, defaultMethod);
                compareVals.put("method", defaultMethod);
            } else {
                String method = (String) compareVals.get(METHOD);
                method = method.toUpperCase();
                if (!EnumUtils.isValidEnum(Comparisons.Method.class, method)) {
                    log.warn("'{}': '{}' passed is not a valid comparison method - will use: {}", simplifiedIdentifier,
                            method, defaultMethod);
                    method = defaultMethod.toString();
                }
                Comparisons.Method methodEnum = Comparisons.Method.valueOf(method);
                compareVals.put(METHOD, methodEnum);
            }

            Comparisons.Method compareMethod = (Comparisons.Method) compareVals.get(METHOD);
            if (compareMethod.equals(Comparisons.Method.SD)) {
                if (!compareVals.containsKey(DEVIATIONS_ALLOWED)) {
                    compareVals.put(DEVIATIONS_ALLOWED, defaultDeviations);
                    log.warn("'{}': Deviations Allowed not specified, will allow: {}", simplifiedIdentifier,
                            defaultDeviations);
                } else {
                    String deviationsStr = (String) compareVals.get(DEVIATIONS_ALLOWED);
                    try {
                        Double deviations = Double.parseDouble(deviationsStr);
                        compareVals.put(DEVIATIONS_ALLOWED, deviationsStr);
                        log.info("'{}': Will compare allowing deviations: {}", simplifiedIdentifier, deviations);
                    } catch (Exception e) {
                        log.warn("'{}': '{}' passed is not a valid number - will use: {}", simplifiedIdentifier,
                                deviationsStr, defaultDeviations);
                        compareVals.put(DEVIATIONS_ALLOWED, defaultDeviations);
                    }
                }
            } else {
                if (compareVals.containsKey(DEVIATIONS_ALLOWED)) {
                    log.warn(
                            "'{}': Deviations allowed were specified, but method was specified as 'DELTA' - comparison does not require deviation values",
                            simplifiedIdentifier);
                    compareVals.remove(DEVIATIONS_ALLOWED);
                }
            }

            if (!compareVals.containsKey(RANGE)) {
                log.warn("'{}': Range not defined, will use: {}", simplifiedIdentifier, defaultRange);
                compareVals.put(RANGE, defaultRange);
            } else {
                String range = (String) compareVals.get(RANGE);
                range = range.toUpperCase();
                if (!range.equals("ALL")) {
                    try {
                        Integer rangeInt = Integer.parseInt(range);
                    } catch (Exception e) {
                        log.warn("'{}': '{}' passed is not a valid comparison range - will use: {}",
                                simplifiedIdentifier, range, defaultRange);
                        range = defaultRange;
                    }
                }
                compareVals.put(RANGE, range);
            }

            if (!compareVals.containsKey(SCOPE)) {
                log.warn("'{}': Scope not defined, will use: {}", simplifiedIdentifier, defaultScope);
                compareVals.put(SCOPE, defaultScope);
            } else {
                String scope = (String) compareVals.get(SCOPE);
                scope = scope.toUpperCase();
                if (!EnumUtils.isValidEnum(Comparisons.Scope.class, scope)) {
                    log.warn("'{}': '{}' passed is not a valid comparison scope - will use: {}", simplifiedIdentifier,
                            scope, defaultScope);
                    scope = defaultScope.toString();
                }
                Comparisons.Scope scopeEnum = Comparisons.Scope.valueOf(scope);
                compareVals.put(SCOPE, scopeEnum);
            }

            Comparisons.Scope compareScope = (Comparisons.Scope) compareVals.get(SCOPE);
            if (compareScope.equals(Comparisons.Scope.BETWEEN)) {
                if (!compareVals.containsKey(COMPARE_VERSION)) {
                    if (defaultVals.containsKey(COMPARE_VERSION)) {
                        compareVals.put(COMPARE_VERSION, defaultVersion);
                        log.warn("'{}': Compare version not specified, will compare to version: {}",
                                simplifiedIdentifier, defaultVersion);
                    } else {
                        log.warn(
                                "'{}': Compare scope recognized as 'BETWEEN' but no compare version was specified - please provide a version to compare to",
                                simplifiedIdentifier);
                        log.warn("'{}': Will use scope: {}", simplifiedIdentifier, defaultScope);
                        compareVals.put(SCOPE, defaultScope);
                        compareVals.remove(COMPARE_VERSION);
                    }
                } else {
                    String version = (String) compareVals.get(COMPARE_VERSION);
                    compareVals.put(COMPARE_VERSION, version);
                    log.info("'{}': Will compare to version: {}", simplifiedIdentifier, version);
                }
            } else {
                if (compareVals.containsKey(COMPARE_VERSION) && compareScope.equals(Comparisons.Scope.WITHIN)) {
                    log.warn(
                            "'{}': Version was specified, but scope was specified as 'WITHIN' - will compare within benchmarked version",
                            simplifiedIdentifier);
                    compareVals.remove(COMPARE_VERSION);
                } else if (compareVals.containsKey(COMPARE_VERSION)) {
                    String version = (String) compareVals.get(COMPARE_VERSION);
                    if (defaultScope.equals(Comparisons.Scope.BETWEEN)) {
                        compareVals.put(COMPARE_VERSION, version);
                        log.info("'{}': Will compare to version: {}", simplifiedIdentifier, version);
                    }
                }
            }

            if (compareMethod.equals(Comparisons.Method.DELTA)) {
                if (!compareVals.containsKey(THRESHOLD)) {
                    log.warn("'{}': Threshold not defined, will use: {}", simplifiedIdentifier, defaultThreshold);
                    compareVals.put(THRESHOLD, defaultThreshold);
                } else {
                    String threshold = (String) compareVals.get(THRESHOLD);
                    threshold = threshold.toUpperCase();
                    if (!EnumUtils.isValidEnum(Comparisons.Threshold.class, threshold)) {
                        log.warn("'{}': '{}' passed is not a valid comparison threshold - will use: {}",
                                simplifiedIdentifier, threshold, defaultThreshold);
                        threshold = defaultThreshold.toString();
                    }
                    Comparisons.Threshold thresholdEnum = Comparisons.Threshold.valueOf(threshold);
                    compareVals.put(THRESHOLD, thresholdEnum);
                }

                Comparisons.Threshold compareThreshold = (Comparisons.Threshold) compareVals.get(THRESHOLD);
                if (compareThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                    if (!compareVals.containsKey(PERCENT_CHANGE_ALLOWED)) {
                        if (defaultVals.containsKey(PERCENT_CHANGE_ALLOWED)) {
                            compareVals.put(PERCENT_CHANGE_ALLOWED, defaultPercentage);
                            log.warn("'{}': Percentage not specified, will use percentage: {}", simplifiedIdentifier,
                                    defaultPercentage);
                        } else {
                            log.warn(
                                    "'{}': Compare threshold recognized as 'PERCENT_CHANGE' but no compare percentage was specified - please provide a percentage to compare with",
                                    simplifiedIdentifier);
                            log.warn("'{}': Will use threshold: {}", simplifiedIdentifier, defaultThreshold);
                            compareVals.put(THRESHOLD, defaultThreshold);
                            compareVals.remove(PERCENT_CHANGE_ALLOWED);
                        }
                    } else {
                        String percentageStr = (String) compareVals.get(PERCENT_CHANGE_ALLOWED);
                        try {
                            Double percentage = Double.parseDouble(percentageStr);
                            compareVals.put(PERCENT_CHANGE_ALLOWED, percentage);
                            log.info("'{}': Will compare with percentage: {}", simplifiedIdentifier, percentage);
                        } catch (Exception e) {
                            log.warn("'{}': '{}' passed is not a valid number - will use: {}", simplifiedIdentifier,
                                    percentageStr, defaultPercentage);
                            compareVals.put(PERCENT_CHANGE_ALLOWED, defaultPercentage);
                        }
                    }
                } else {
                    if (compareVals.containsKey(PERCENT_CHANGE_ALLOWED)
                            && compareThreshold.equals(Comparisons.Threshold.GREATER)) {
                        log.warn(
                                "'{}': Percentage was specified, but threshold was specified as 'GREATER' - will compare using the 'GREATER' threshold and ignore percentage value",
                                simplifiedIdentifier);
                        compareVals.remove(PERCENT_CHANGE_ALLOWED);
                    } else if (compareVals.containsKey(PERCENT_CHANGE_ALLOWED)) {
                        String percentageStr = (String) compareVals.get(PERCENT_CHANGE_ALLOWED);
                        try {
                            Double percentage = Double.parseDouble(percentageStr);
                            if (defaultThreshold.equals(Comparisons.Threshold.PERCENT_CHANGE)) {
                                compareVals.put(PERCENT_CHANGE_ALLOWED, percentage);
                                log.info("'{}': Will compare with percentage: {}", simplifiedIdentifier, percentage);
                            }
                        } catch (Exception e) {
                            log.warn("'{}': '{}' passed is not a valid number - will use: {}", simplifiedIdentifier,
                                    percentageStr, defaultPercentage);
                            compareVals.put(PERCENT_CHANGE_ALLOWED, defaultPercentage);
                        }

                    }
                }
            } else {
                if (compareVals.containsKey(PERCENT_CHANGE_ALLOWED)) {
                    log.warn(
                            "'{}': Percentage was specified but method was specified as 'SD' - will ignore percentage and only compare using deviations allowed",
                            simplifiedIdentifier);
                    compareVals.remove(PERCENT_CHANGE_ALLOWED);
                }
            }

            comparatorProps.put(identifier, compareVals);
            if (identifier.equals("MyScript")) {
                packageName = "MyScript";
            } else if (compareVals.containsKey("package")) {
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
