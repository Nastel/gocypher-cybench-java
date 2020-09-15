/*
 * Copyright (c) 2018-2020 K2N.IO. All Rights Reserved.
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
				confFilePath = new File("").getAbsolutePath()+"/conf/gocypher-benchmark-client-configuration.properties";
			}else{
				confFilePath = filePath;
			}
			prop.load(new FileInputStream( confFilePath));
			LOG.info("** Configuration loaded: {}", confFilePath);
			return prop;
		} catch (Exception e) {
			LOG.error("Configuration file provided not found, will try to use default configuration file.");
			try{
				confFilePath = new File("").getAbsolutePath()+"/conf/gocypher-benchmark-client-configuration.properties";
				prop.load(new FileInputStream( confFilePath));
				return prop;
			}catch (Exception err){
				LOG.error("ALERT: Default configuration file is missing, try re-downloading the project or use our documentation to create your own configuration file");
			}
		}
		return new Properties();
	}
}
