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

package com.gocypher.benchmarks.runner.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigurationHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationHandler.class);

	public static Properties loadConfiguration(String filePath) {
		Properties prop = new Properties();
		String confFilePath;
		try {
			if(filePath.isEmpty()) {
				confFilePath = new File("").getAbsolutePath()+"/conf/cybench-launcher.properties";
			}else{
				confFilePath = filePath;
			}
			prop.load(new FileInputStream( confFilePath));
			LOG.info("** Configuration loaded: {}", confFilePath);
			return prop;
		} catch (Exception e) {
			LOG.error("Configuration file provided not found, will try to use default configuration file.");
			try{
				confFilePath = new File("").getAbsolutePath()+"/conf/cybench-launcher.properties";
				prop.load(new FileInputStream( confFilePath));
				return prop;
			}catch (Exception err){
				LOG.error("ALERT: Default configuration file is missing, try re-downloading the project or use our documentation to create your own configuration file");
			}
		}
		return new Properties();
	}
}
