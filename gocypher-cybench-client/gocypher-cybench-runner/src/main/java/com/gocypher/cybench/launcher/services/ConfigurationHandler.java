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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationHandler.class);

    private static final String CONFIG_FILE = System.getProperty("cybench.config.file",
            "/conf/cybench-launcher.properties");

    public static Properties loadConfiguration(String filePath) {
        Properties prop = new Properties();
        String confFilePath = filePath;
        try {
            if (filePath.isEmpty()) {
                confFilePath = new File("").getAbsolutePath() + CONFIG_FILE;
            } else {
                confFilePath = filePath;
            }
            try (InputStream cfIn = new FileInputStream(confFilePath)) {
                prop.load(cfIn);
            }
            LOG.info("** Configuration loaded: {}", confFilePath);
            return prop;
        } catch (Exception e) {
            LOG.error("Configuration file={} not found, will try to use default configuration", confFilePath, e);
            try {
                confFilePath = new File("").getAbsolutePath() + CONFIG_FILE;
                try (InputStream cfIn = new FileInputStream(confFilePath)) {
                    prop.load(cfIn);
                }
                return prop;
            } catch (Exception err) {
                LOG.error(
                        "Default configuration file is missing, try re-downloading the project or use our documentation to create your own configuration file={}",
                        confFilePath, err);
            }
        }
        return new Properties();
    }
}
