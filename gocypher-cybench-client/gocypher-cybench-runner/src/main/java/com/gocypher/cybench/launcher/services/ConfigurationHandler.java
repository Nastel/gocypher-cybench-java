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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationHandler.class);

    private static final String CONFIG_FILE = System.getProperty("cybench.config.file",
            "/config/cybench-launcher.properties");

    public static Properties loadConfiguration(String filePath) {
        Properties prop = new Properties();
        String confFilePath = filePath;

        try {
            confFilePath = getCfgPath(filePath);
            try (InputStream cfIn = new FileInputStream(confFilePath)) {
                prop.load(cfIn);
            }
            LOG.info("** Configuration loaded: {}", confFilePath);
        } catch (Exception e) {
            LOG.error("Failed to load configuration from file file={}", confFilePath, e);
        }

        return prop;
    }

    protected static String getCfgPath(String confFilePath) {
        File cfgFile = new File(confFilePath);
        if (StringUtils.isEmpty(confFilePath) || !cfgFile.exists()) {
            confFilePath = new File("").getAbsolutePath() + CONFIG_FILE;
        }

        return confFilePath;
    }
}
