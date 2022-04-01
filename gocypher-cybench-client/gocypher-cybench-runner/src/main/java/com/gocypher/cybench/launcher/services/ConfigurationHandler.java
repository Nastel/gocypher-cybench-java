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
import java.util.HashMap;
import java.util.Properties;

import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Method;
import com.gocypher.cybench.model.ComparisonConfig.Scope;
import com.gocypher.cybench.model.ComparisonConfig.Threshold;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationHandler.class);

    private static final String LAUNCHER_CONFIG_FILE = System.getProperty("cybench.config.file",
            "/config/cybench-launcher.properties");

    private static final String AUTOMATIC_COMPARISON_CONFIG_FILE = System.getProperty("cybench.automation.file",
            "/config/cybench-automation.properties");

    public static Properties loadConfiguration(String filePath, String configurationFileToLookFor) {
        Properties prop = new Properties();
        String confFilePath = filePath;

        try {
            if (configurationFileToLookFor.equals(Constants.LAUNCHER_CONFIGURATION)) {
                confFilePath = getCfgPath(filePath, LAUNCHER_CONFIG_FILE);
            } else if (configurationFileToLookFor.equals(Constants.AUTOMATED_COMPARISON_CONFIGURATION)) {
                confFilePath = getCfgPath(filePath, AUTOMATIC_COMPARISON_CONFIG_FILE);
            }
            try (InputStream cfIn = new FileInputStream(confFilePath)) {
                prop.load(cfIn);
            }
            LOG.info("** Configuration loaded: {}", confFilePath);
        } catch (Exception e) {
            LOG.error("Failed to load configuration from file file={}", confFilePath, e);
        }

        return prop;
    }

    protected static String getCfgPath(String confFilePath, String config_file) {
        File cfgFile = new File(confFilePath);
        if (StringUtils.isEmpty(confFilePath) || !cfgFile.exists()) {
            confFilePath = new File("").getAbsolutePath() + config_file;
        }

        return confFilePath;
    }

    public static ComparisonConfig checkConfigValidity(Properties automatedComparisonCfgProps) {
        try {
            ComparisonConfig automatedComparisonConfig = new ComparisonConfig();

            String SCOPE_STR = (String) automatedComparisonCfgProps.get("scope");
            Scope SCOPE;
            String COMPARE_VERSION = (String) automatedComparisonCfgProps.get("compareVersion");
            Integer NUM_LATEST_REPORTS = (Integer) automatedComparisonCfgProps.get("numLatestReports");
            Integer ANOMALIES_ALLOWED = (Integer) automatedComparisonCfgProps.get("anomaliesAllowed");
            String METHOD_STR = (String) automatedComparisonCfgProps.get("method");
            Method METHOD;
            String THRESHOLD_STR = (String) automatedComparisonCfgProps.get("threshold");
            Threshold THRESHOLD;
            Double PERCENT_CHANGE_ALLOWED = (Double) automatedComparisonCfgProps.get("percentChangeAllowed");
            Double DEVIATIONS_ALLOWED = (Double) automatedComparisonCfgProps.get("deviationsAllowed");
            
            
            if (NUM_LATEST_REPORTS == null || NUM_LATEST_REPORTS < 1) {
                throw new Exception("Not enought latest reports specified to compare to!");
            }
            if (ANOMALIES_ALLOWED == null || ANOMALIES_ALLOWED < 1) {
                throw new Exception("Not enought anomalies allowed specified!");
            }
            automatedComparisonConfig.setCompareLatestReports(NUM_LATEST_REPORTS);
            automatedComparisonConfig.setAnomaliesAllowed(ANOMALIES_ALLOWED);

            if (!EnumUtils.isValidEnum(Scope.class, SCOPE_STR) || StringUtils.isBlank(SCOPE_STR)) {
                throw new Exception("No scope specified or scope is invalid!");
            } else {
                SCOPE = Scope.valueOf(SCOPE_STR);
                automatedComparisonConfig.setScope(SCOPE);
            }
            if (!EnumUtils.isValidEnum(Method.class, METHOD_STR) || StringUtils.isBlank(METHOD_STR)) {
                throw new Exception("No method specified or method is invalid!");
            } else {
                METHOD = Method.valueOf(METHOD_STR);
                automatedComparisonConfig.setMethod(METHOD);
            }

            if (SCOPE.equals(Scope.WITHIN) && StringUtils.isNotEmpty(COMPARE_VERSION)) {
                COMPARE_VERSION = "";
                LOG.warn("Automated comparison config scoped specified as WITHIN but compare version was also specified, will compare WITHIN the currently tested version.");
            } else if (SCOPE.equals(Scope.BETWEEN) && StringUtils.isBlank(COMPARE_VERSION)) {
                throw new Exception("Scope specified as BETWEEN but no compare version specified!");
            } else if (SCOPE.equals(Scope.BETWEEN)) {
                automatedComparisonConfig.setCompareVersion(COMPARE_VERSION);
            }

            if (METHOD.equals(Method.SD) && (DEVIATIONS_ALLOWED == null || DEVIATIONS_ALLOWED <= 0)) {
                throw new Exception("Method specified as SD but not enough deviations allowed were specified!");
            } else if(METHOD.equals(Method.SD)) {
                automatedComparisonConfig.setDeviationsAllowed(DEVIATIONS_ALLOWED);
            } else if (METHOD.equals(Method.DELTA)) {
                if (!EnumUtils.isValidEnum(Threshold.class, THRESHOLD_STR) || StringUtils.isBlank(THRESHOLD_STR)) {
                    throw new Exception("Method specified as DELTA but no threshold specified or threshold is invalid!");
                } else {
                    THRESHOLD = Threshold.valueOf(THRESHOLD_STR);
                    automatedComparisonConfig.setThreshold(THRESHOLD);
                }

                if (THRESHOLD.equals(Threshold.PERCENT_CHANGE) && (PERCENT_CHANGE_ALLOWED == null || PERCENT_CHANGE_ALLOWED <= 0)) {
                    throw new Exception("Threshold specified as PERCENT_CHANGE but percent change allowed was either not specified or not high enough!");
                } else if (THRESHOLD.equals(Threshold.PERCENT_CHANGE)) {
                    automatedComparisonConfig.setPercentChangeAllowed(PERCENT_CHANGE_ALLOWED);
                }
            }
            
            return automatedComparisonConfig;
        } catch (Exception e) {
            LOG.error("Failed to parse automated comparison configurations", e);
            return null;
        }
    }
}
