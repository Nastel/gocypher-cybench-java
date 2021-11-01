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
import com.gocypher.cybench.services.Requests;

public class ComparatorScriptEngine {

    private static final Logger log = LoggerFactory.getLogger(ComparatorScriptEngine.class);
    private final String[] engineDefs = { "var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');",
            "var Requests = Java.type('com.gocypher.cybench.services.Requests');",
            "var forEach = Array.prototype.forEach;", "var HashMap = Java.type('java.util.HashMap');",
            "var ArrayList = Java.type('java.util.ArrayList');" };
    private Map<String, Map<String, Map<String, Double>>> myBenchmarks;
    private ArrayList<String> myFingerprints;
    private Map<String, Object> passedProps;

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
        
        initiateFetch(token, reportPath);
        if (handleComparatorConfigs(method, range, scope, compareVersion, threshold, percentChangeAllowed,
                deviationsAllowed)) {
	        File userScript = loadUserScript(scriptPath);
	        ScriptEngine engine = prepareScriptEngine();
	        runUserScript(engine, userScript);
        } else {
        	log.warn("No comparisons can be run with invalid configurations!");
        }
    }

    private void initiateFetch(String token, String reportPath) {
        if (token == null) {
            log.warn("No access token provided!");
            token = ConfigHandling.DEFAULT_TOKEN;
        }

        if (reportPath == null) {
            log.warn("No report location provided, using default: {}", ConfigHandling.DEFAULT_REPORTS_LOCATION);
            reportPath = ConfigHandling.DEFAULT_REPORTS_LOCATION;
        }
        myBenchmarks = Requests.getBenchmarksFromReport(token, ConfigHandling.identifyRecentReport(reportPath));
        myFingerprints = new ArrayList<>(myBenchmarks.keySet());
    }

    private boolean handleComparatorConfigs(String method, String range, String scope, String compareVersion,
            String threshold, String percentChangeAllowed, String deviationsAllowed) {
        Map<String, Object> comparatorProps = new HashMap<>();
        comparatorProps.put(ConfigHandling.DEFAULT_IDENTIFIER_HEADER, ConfigHandling.loadDefaults());

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
        
        if (ConfigHandling.checkConfigValidity("MyScript", comparatorProps))
        	return true;
        return false;
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
            engine.put("myFingerprints", myFingerprints);
            engine.put("fingerprintsToNames", Requests.fingerprintsToNames);
            engine.put("logConfigs", passedProps);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logPassFailData();
    }
    
    private void logPassFailData() throws Exception {
    	System.out.print("\n");
        log.info("compared={}, passed={}, failed={}", CompareBenchmarks.totalComparedBenchmarks, CompareBenchmarks.totalPassedBenchmarks,
        		CompareBenchmarks.totalFailedBenchmarks);
        System.out.print("\n");
        CompareBenchmarks.buildFailureCheck();
    }
}
