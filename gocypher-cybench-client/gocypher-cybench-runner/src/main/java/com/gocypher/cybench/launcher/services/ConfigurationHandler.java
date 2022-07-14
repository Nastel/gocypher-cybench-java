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

package com.gocypher.cybench.launcher.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Method;
import com.gocypher.cybench.model.ComparisonConfig.Scope;
import com.gocypher.cybench.model.ComparisonConfig.Threshold;

public class ConfigurationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationHandler.class);

    private static final String LAUNCHER_CONFIG_FILE = System.getProperty("cybench.config.file",
            "/config/cybench-launcher.properties");

    private static final String AUTOMATIC_COMPARISON_CONFIG_FILE = System.getProperty("cybench.automation.file",
            "/config/cybench-automation.properties");

    public static Properties loadConfiguration(String filePath, String configurationFileToLookFor) {
        Properties prop = new Properties();
        File confFile = null;

        try {
            if (configurationFileToLookFor.equals(Constants.LAUNCHER_CONFIGURATION)) {
                confFile = getCfgPath(filePath, LAUNCHER_CONFIG_FILE);
            } else if (configurationFileToLookFor.equals(Constants.AUTOMATED_COMPARISON_CONFIGURATION)) {
                confFile = getCfgPath(filePath, AUTOMATIC_COMPARISON_CONFIG_FILE);
            } else {
                confFile = new File(filePath);
            }

            if (confFile.exists()) {
                try (InputStream cfIn = new FileInputStream(confFile)) {
                    prop.load(cfIn);
                }
                LOG.info("** Configuration loaded: {}", confFile.getPath());
            } else {
                LOG.warn("** Configuration file is missing: {}", confFile.getPath());
            }
        } catch (Exception e) {
            LOG.error("Failed to load configuration from file: path={}",
                    confFile == null ? filePath : confFile.getPath(), e);
        }

        return prop;
    }

    protected static File getCfgPath(String confFilePath, String configFile) {
        File cfgFile = new File(confFilePath);
        if (StringUtils.isEmpty(confFilePath) || !cfgFile.exists()) {
            // TODO: better approach may be possible, but this works.
            cfgFile = new File(new File("").getAbsolutePath() + configFile);
        }

        return cfgFile;
    }

    public static ComparisonConfig checkConfigValidity(Properties automatedComparisonCfgProps) {
        try {
            ComparisonConfig automatedComparisonConfig = new ComparisonConfig();

            if (automatedComparisonCfgProps.containsKey(Constants.AUTO_SHOULD_RUN_COMPARISON)) {
                boolean shouldRunComparison = Boolean
                        .parseBoolean((String) automatedComparisonCfgProps.get(Constants.AUTO_SHOULD_RUN_COMPARISON));
                automatedComparisonConfig.setShouldRunComparison(shouldRunComparison);
            }

            String SCOPE_STR = (String) automatedComparisonCfgProps.get(Constants.AUTO_SCOPE);
            if (StringUtils.isBlank(SCOPE_STR)) {
                throw new Exception("Scope is not specified!");
            } else {
                SCOPE_STR = SCOPE_STR.toUpperCase();
            }
            Scope SCOPE;
            String COMPARE_VERSION = (String) automatedComparisonCfgProps.get(Constants.AUTO_COMPARE_VERSION);
            String NUM_LATEST_REPORTS_STR = (String) automatedComparisonCfgProps.get(Constants.AUTO_LATEST_REPORTS);
            String ANOMALIES_ALLOWED_STR = (String) automatedComparisonCfgProps.get(Constants.AUTO_ANOMALIES_ALLOWED);
            String METHOD_STR = (String) automatedComparisonCfgProps.get(Constants.AUTO_METHOD);
            if (StringUtils.isBlank(METHOD_STR)) {
                throw new Exception("Method is not specified!");
            } else {
                METHOD_STR = METHOD_STR.toUpperCase();
            }
            Method METHOD;
            String THRESHOLD_STR = (String) automatedComparisonCfgProps.get(Constants.AUTO_THRESHOLD);
            if (StringUtils.isNotBlank(THRESHOLD_STR)) {
                THRESHOLD_STR = THRESHOLD_STR.toUpperCase();
            }
            Threshold THRESHOLD;
            String PERCENT_CHANGE_ALLOWED_STR = (String) automatedComparisonCfgProps.get(Constants.AUTO_PERCENT_CHANGE);
            String DEVIATIONS_ALLOWED_STR = (String) automatedComparisonCfgProps.get(Constants.AUTO_DEVIATIONS_ALLOWED);

            if (StringUtils.isNotBlank(NUM_LATEST_REPORTS_STR)) {
                int NUM_LATEST_REPORTS = Integer.parseInt(NUM_LATEST_REPORTS_STR);
                if (NUM_LATEST_REPORTS < 1) {
                    throw new Exception("Not enough latest reports specified to compare to!");
                }
                automatedComparisonConfig.setCompareLatestReports(NUM_LATEST_REPORTS);
            } else {
                throw new Exception("Number of latest reports to compare to was not specified!");
            }
            if (StringUtils.isNotBlank(ANOMALIES_ALLOWED_STR)) {
                int ANOMALIES_ALLOWED = Integer.parseInt(ANOMALIES_ALLOWED_STR);
                if (ANOMALIES_ALLOWED < 0) {
                    throw new Exception("Not enough anomalies allowed specified!");
                }
                automatedComparisonConfig.setAnomaliesAllowed(ANOMALIES_ALLOWED);
            } else {
                throw new Exception("Anomalies allowed was not specified!");
            }

            if (!EnumUtils.isValidEnum(Scope.class, SCOPE_STR)) {
                throw new Exception("Scope is invalid!");
            } else {
                SCOPE = Scope.valueOf(SCOPE_STR);
                automatedComparisonConfig.setScope(SCOPE);
            }
            if (!EnumUtils.isValidEnum(Method.class, METHOD_STR)) {
                throw new Exception("Method is invalid!");
            } else {
                METHOD = Method.valueOf(METHOD_STR);
                automatedComparisonConfig.setMethod(METHOD);
            }

            if (SCOPE.equals(Scope.WITHIN) && StringUtils.isNotEmpty(COMPARE_VERSION)) {
                COMPARE_VERSION = "";
                LOG.warn(
                        "Automated comparison config scoped specified as WITHIN but compare version was also specified, will compare WITHIN the currently tested version.");
            } else if (SCOPE.equals(Scope.BETWEEN) && StringUtils.isBlank(COMPARE_VERSION)) {
                throw new Exception("Scope specified as BETWEEN but no compare version specified!");
            } else if (SCOPE.equals(Scope.BETWEEN)) {
                automatedComparisonConfig.setCompareVersion(COMPARE_VERSION);
            }

            if (METHOD.equals(Method.SD)) {
                if (StringUtils.isNotBlank(DEVIATIONS_ALLOWED_STR)) {
                    double DEVIATIONS_ALLOWED = Double.parseDouble(DEVIATIONS_ALLOWED_STR);
                    if (DEVIATIONS_ALLOWED <= 0) {
                        throw new Exception("Method specified as SD but not enough deviations allowed were specified!");
                    }
                    automatedComparisonConfig.setDeviationsAllowed(DEVIATIONS_ALLOWED);
                } else {
                    throw new Exception("Method specified as SD but deviations allowed was not specified!");
                }
                if (automatedComparisonConfig.getCompareLatestReports() < 2) {
                    throw new Exception(
                            "Method SD requires at least 2 reports to compare against! Not enough latest reports specified to compare to!");
                }
            } else if (METHOD.equals(Method.DELTA)) {
                if (!EnumUtils.isValidEnum(Threshold.class, THRESHOLD_STR) || StringUtils.isBlank(THRESHOLD_STR)) {
                    throw new Exception(
                            "Method specified as DELTA but no threshold specified or threshold is invalid!");
                } else {
                    THRESHOLD = Threshold.valueOf(THRESHOLD_STR);
                    automatedComparisonConfig.setThreshold(THRESHOLD);
                }

                if (THRESHOLD.equals(Threshold.PERCENT_CHANGE)) {
                    if (StringUtils.isNotBlank(PERCENT_CHANGE_ALLOWED_STR)) {
                        double PERCENT_CHANGE_ALLOWED = Double.parseDouble(PERCENT_CHANGE_ALLOWED_STR);
                        if (PERCENT_CHANGE_ALLOWED <= 0) {
                            throw new Exception(
                                    "Threshold specified as PERCENT_CHANGE but percent change is not high enough!");
                        }
                        automatedComparisonConfig.setPercentChangeAllowed(PERCENT_CHANGE_ALLOWED);
                    } else {
                        throw new Exception(
                                "Threshold specified as PERCENT_CHANGE but percent change allowed was not specified!");
                    }
                }
            }

            return automatedComparisonConfig;
        } catch (Exception e) {
            LOG.error("Failed to parse automated comparison configuration", e);
            return null;
        }
    }
}
