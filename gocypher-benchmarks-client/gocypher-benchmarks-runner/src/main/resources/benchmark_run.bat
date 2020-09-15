::
:: Copyright (c) 2018-2020 K2N.IO. All Rights Reserved.
::
:: This software is the confidential and proprietary information of
:: K2N.IO. ("Confidential Information").  You shall not disclose
:: such Confidential Information and shall use it only in accordance with
:: the terms of the license agreement you entered into with K2N.IO.
::
:: K2N.IO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
:: THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
:: THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
:: PURPOSE, OR NON-INFRINGEMENT. K2N.IO SHALL NOT BE LIABLE FOR ANY DAMAGES
:: SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
:: THIS SOFTWARE OR ITS DERIVATIVES.
::
:: CopyrightVersion 1.0

@echo off
@setlocal enableextensions enabledelayedexpansion
echo -

:: Read user input to set the path to java to execute and if there is a need a path to user configurations file for JVM properties and benchmarks functionality
IF NOT ["%JAVA_HOME%"] EQU [""] set /p JAVA_PATH= Enter the path of java to test or leave default ([%JAVA_HOME%\bin\java.exe])?:
IF ["%JAVA_HOME%"] EQU [""] set /p JAVA_PATH= Enter the path of java to test or leave default ([%JAVA_HOME%\bin\java.exe])?:
IF ["%JAVA_PATH%"] EQU [""] set JAVA_PATH=java
echo -
set /p CONFIGURATION_PATH= Provide full path to configuration file or use default ([conf\gocypher-benchmark-client-configuration.properties])?:
IF ["%CONFIGURATION_PATH%"] EQU [""] set CONFIGURATION_PATH=conf/gocypher-benchmark-client-configuration.properties

:: Read properties file to set JVM properties for .jar run
echo -
for /f "delims== tokens=1,2" %%A in (%CONFIGURATION_PATH%) do (
	Echo."%%A" | findstr /C:"JVM">nul && (
	  set JVM_PROPERTIES=!JVM_PROPERTIES!%%B
		set JVM_PROPERTIES=!JVM_PROPERTIES!
	)
)

:: Execute the benchmarks with set default or user defined properties
IF ["%JAVA_PATH%"] EQU ["java"] (
	echo EXECUTE: java %JVM_PROPERTIES% -jar ./gocypher-benchmarks-client.jar cfg=%CONFIGURATION_PATH%
	java %JVM_PROPERTIES% -jar ./gocypher-benchmarks-client.jar cfg=%CONFIGURATION_PATH%
)
IF NOT ["%JAVA_PATH%"] EQU ["java"] (
	echo EXECUTE: "%JAVA_PATH%" %JVM_PROPERTIES% -jar ./gocypher-benchmarks-client.jar cfg=%CONFIGURATION_PATH%
	"%JAVA_PATH%" %JVM_PROPERTIES% -jar ./gocypher-benchmarks-client.jar cfg=%CONFIGURATION_PATH%
)