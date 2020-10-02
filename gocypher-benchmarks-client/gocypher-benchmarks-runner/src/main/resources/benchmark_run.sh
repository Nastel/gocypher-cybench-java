#
# Copyright (c) 2018-2020 K2N.IO. All Rights Reserved.
#
# This software is the confidential and proprietary information of
# K2N.IO. ("Confidential Information").  You shall not disclose
# such Confidential Information and shall use it only in accordance with
# the terms of the license agreement you entered into with K2N.IO.
#
# K2N.IO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
# THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
# THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE, OR NON-INFRINGEMENT. K2N.IO SHALL NOT BE LIABLE FOR ANY DAMAGES
# SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
# THIS SOFTWARE OR ITS DERIVATIVES.
#
# CopyrightVersion 1.0
#
#!/bin/bash

# Method to ask for user input and save the response to value
prompt_token() {
  local VAL=""
  if [ "$VAL" = "" ]; then
    echo -n "${2:-$1} : "
    read VAL
  fi
  VAL=$(printf '%q' "$VAL")
  eval $1=$VAL
}

# Read user input to set the path to java to execute and if there is a need a path to user configurations file for JVM properties and benchmarks functionality
echo "-"
echo "Do you have more then one java installed? Specify the version else the script will try to run using the default settings"
echo "No java present? Press Ctrl-C -> download java -> try again!"

prompt_token 'JAVA_PATH'          '               Full path to java execute'
echo "-"
echo "Change the configuration file to run or leave empty for default:"
prompt_token 'CONFIGURATION_PATH'          '               Configuration file'

if [ "$CONFIGURATION_PATH" = "" ]
then
    CONFIGURATION_PATH="conf/gocypher-benchmark-client-configuration.properties"
fi

# Read properties file to set JVM properties for .jar run
JVM_PROPERTIES=""
while IFS='=' read -r key value; do
    if [[ ${key} == "javaOptions"* ]]; then
        JVM_PROPERTIES+="${value} ";
    fi
done < ${CONFIGURATION_PATH}

# Read properties file to try to set JAVA_PATH from confgiuration file if not provided during runtime.
JAVA_PATH=""
while IFS='=' read -r key value; do
    if [[ ${key} == "javaToUsePath"* ]]; then
        if [[ -z "${JAVA_PATH}" ]];then
        JAVA_PATH="${value}";
        fi
    fi
done < ${CONFIGURATION_PATH}

# Execute the benchmarks with set default or user defined properties
if [[ -z "${JAVA_PATH}" ]];then
    echo java ${JVM_PROPERTIES} -jar ./gocypher-benchmarks-client.jar "$CONFIGURATION_PATH"
    java ${JVM_PROPERTIES} -jar ./gocypher-benchmarks-client.jar "$CONFIGURATION_PATH"
else
    echo "$JAVA_PATH" ${JVM_PROPERTIES} -jar ./gocypher-benchmarks-client.jar "$CONFIGURATION_PATH"
    "$JAVA_PATH" ${JVM_PROPERTIES} -jar ./gocypher-benchmarks-client.jar "$CONFIGURATION_PATH"
fi
