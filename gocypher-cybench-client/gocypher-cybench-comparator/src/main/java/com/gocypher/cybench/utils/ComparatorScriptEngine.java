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

package com.gocypher.cybench.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.CompareBenchmarks;
import com.gocypher.cybench.model.ComparedBenchmark;
import com.gocypher.cybench.services.Requests;

// TODO NEEDS PATCH
public class ComparatorScriptEngine {
    private static final Logger log = LoggerFactory.getLogger(ComparatorScriptEngine.class);

    private final String[] engineDefs = { "var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');",
            "var CompareBenchmarks = Java.type('com.gocypher.cybench.CompareBenchmarks');",
            "var ConfigHandling = Java.type('com.gocypher.cybench.utils.ConfigHandling');",
            "var ComparedBenchmark = Java.type('com.gocypher.cybench.model.ComparedBenchmark');",
            "var ComparisonConfig = Java.type('com.gocypher.cybench.model.ComparisonConfig');",
            "var forEach = Array.prototype.forEach;", "var HashMap = Java.type('java.util.HashMap');",
            "var ArrayList = Java.type('java.util.ArrayList');",
            "var Double = Java.type('java.lang.Double');" };
    private ArrayList<ComparedBenchmark> myBenchmarks;
    private Map<String, Object> passedProps;

    public static Map<String, Object> comparatorProps;

    public ComparatorScriptEngine(Map<String, String> passedProps, String scriptPath) throws Exception {
        String token = passedProps.get(ConfigHandling.TOKEN);
        String reportPath = passedProps.get(ConfigHandling.REPORT_PATH);
        String method = passedProps.get(ConfigHandling.METHOD);
        String range = passedProps.get(ConfigHandling.RANGE);
        String scope = passedProps.get(ConfigHandling.SCOPE);
        String compareVersion = passedProps.get(ConfigHandling.COMPARE_VERSION);
        String threshold = passedProps.get(ConfigHandling.THRESHOLD);
        String percentChangeAllowed = passedProps.get(ConfigHandling.PERCENT_CHANGE_ALLOWED);
        String deviationsAllowed = passedProps.get(ConfigHandling.DEVIATIONS_ALLOWED);
        comparatorProps = new HashMap<>();

        if (parseReportAndFetchProjectInfo(token, reportPath)) {
            if (handleComparatorConfigs(method, range, scope, compareVersion, threshold, percentChangeAllowed,
                    deviationsAllowed)) {
                File userScript = loadUserScript(scriptPath);
                ScriptEngine engine = prepareScriptEngine();
                runUserScript(engine, userScript);
            } else {
                log.error("No comparisons can be run with invalid configurations!");
            }
        }
    }

    private boolean parseReportAndFetchProjectInfo(String token, String reportPath) {
        if (token == null) {
            log.warn("No access token provided!");
            token = ConfigHandling.DEFAULT_TOKEN;
        }
        CompareBenchmarks.setAccessToken(token);

        if (reportPath == null) {
            log.warn("No report location provided, using default: {}", ConfigHandling.DEFAULT_REPORTS_LOCATION);
            reportPath = ConfigHandling.DEFAULT_REPORTS_LOCATION;
        }
        myBenchmarks = condenseRecentBenchmarksToList(
                Requests.parseRecentReport(ConfigHandling.identifyRecentReport(reportPath)));
        if (myBenchmarks.isEmpty()) {
            log.warn("No benchmarks found in report!");
            return false;
        }
        return Requests.getInstance().getProjectSummary(Requests.project, token);
    }

    private ArrayList<ComparedBenchmark> condenseRecentBenchmarksToList(
            Map<String, Map<String, ComparedBenchmark>> recentBenchmarks) {
        ArrayList<ComparedBenchmark> myBenchmarks = new ArrayList<>();
        for (Map<String, ComparedBenchmark> entry : recentBenchmarks.values()) {
            myBenchmarks.addAll(entry.values());
        }
        return myBenchmarks;
    }

    private boolean handleComparatorConfigs(String method, String range, String scope, String compareVersion,
            String threshold, String percentChangeAllowed, String deviationsAllowed) {

        log.info("Attempting to set comparatorProps");

        comparatorProps.put(ConfigHandling.DEFAULT_IDENTIFIER_HEADER, ConfigHandling.loadDefaults());

        log.info("Finished setting comparatorProps");
        passedProps = new HashMap<>();

        if (StringUtils.isNotEmpty(method)) {
            passedProps.put(ConfigHandling.METHOD, method);
        }
        if (StringUtils.isNotEmpty(range)) {
            passedProps.put(ConfigHandling.RANGE, range);
        }
        if (StringUtils.isNotEmpty(scope)) {
            passedProps.put(ConfigHandling.SCOPE, scope);
        }
        if (StringUtils.isNotEmpty(compareVersion)) {
            passedProps.put(ConfigHandling.COMPARE_VERSION, compareVersion);
        }
        if (StringUtils.isNotEmpty(threshold)) {
            passedProps.put(ConfigHandling.THRESHOLD, threshold);
        }
        if (StringUtils.isNotEmpty(percentChangeAllowed)) {
            passedProps.put(ConfigHandling.PERCENT_CHANGE_ALLOWED, percentChangeAllowed);
        }
        if (StringUtils.isNotEmpty(deviationsAllowed)) {
            passedProps.put(ConfigHandling.DEVIATIONS_ALLOWED, deviationsAllowed);
        }
        comparatorProps.put("MyScript", passedProps);

        return ConfigHandling.checkConfigValidity("MyScript", comparatorProps);
    }

    private File loadUserScript(String scriptPath) {
        File userScript = new File(scriptPath);
        return userScript;
    }

    private ScriptEngine prepareScriptEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn");
        try {
            for (String engineDef : engineDefs) {
                engine.eval(engineDef);
            }

            engine.put("myBenchmarks", myBenchmarks);
            engine.put("configMap", passedProps);
            engine.put("currentVersion", Requests.currentVersion);
            engine.put("latestVersion", Requests.latestVersion);
            engine.put("previousVersion", Requests.previousVersion);
            engine.put("project", Requests.project);
            engine.put(ConfigHandling.METHOD, passedProps.get(ConfigHandling.METHOD));
            engine.put(ConfigHandling.SCOPE, passedProps.get(ConfigHandling.SCOPE));
            engine.put(ConfigHandling.RANGE, passedProps.get(ConfigHandling.RANGE));
            engine.put(ConfigHandling.THRESHOLD, passedProps.get(ConfigHandling.THRESHOLD));
            engine.put(ConfigHandling.PERCENT_CHANGE_ALLOWED, passedProps.get(ConfigHandling.PERCENT_CHANGE_ALLOWED));
            engine.put(ConfigHandling.DEVIATIONS_ALLOWED, passedProps.get(ConfigHandling.DEVIATIONS_ALLOWED));
            engine.put(ConfigHandling.COMPARE_VERSION, passedProps.get(ConfigHandling.COMPARE_VERSION));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/ComparatorScriptBindings.js")));
            engine.eval(reader);
        } catch (Exception se) {
            se.printStackTrace();
        }
        return engine;
    }

    private void runUserScript(ScriptEngine engine, File script) throws Exception {
        Reader reader;
        try {
            reader = new FileReader(script);
            engine.eval(reader);
            CompareBenchmarks.logResults();
            WebpageGenerator.generatePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}