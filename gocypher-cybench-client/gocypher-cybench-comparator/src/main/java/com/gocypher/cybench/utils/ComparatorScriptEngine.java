package com.gocypher.cybench.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.services.Requests;

public class ComparatorScriptEngine {

    private static final Logger log = LoggerFactory.getLogger(ComparatorScriptEngine.class);
    private final String[] engineDefs = { "var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');",
            "var Requests = Java.type('com.gocypher.cybench.services.Requests');",
            "var forEach = Array.prototype.forEach;", "var HashMap = Java.type('java.util.HashMap');",
            "var ArrayList = Java.type('java.util.ArrayList');" };
    private Map<String, Map<String, List<String>>> myBenchmarks;
    private ArrayList<String> myFingerprints;
    private Map<String, Object> passedProps;

    public ComparatorScriptEngine(Map<String, String> passedProps) {
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
        handleComparatorConfigs(method, range, scope, compareVersion, threshold, percentChangeAllowed, deviationsAllowed);
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
        myBenchmarks = Requests.getBenchmarksFromReport(token, reportPath);
        myFingerprints = new ArrayList<>(myBenchmarks.keySet());
    }

    private void handleComparatorConfigs(String method, String range, String scope, String compareVersion, 
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
        if(StringUtils.isNotEmpty(compareVersion)) {
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
        ConfigHandling.checkConfigValidity("MyScript", comparatorProps);
    }

    public File loadUserScript(String scriptPath) {
        File userScript = new File(scriptPath);
        return userScript;
    }

    public ScriptEngine prepareScriptEngine() {
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

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/ComparatorScriptBindings.js")));
            engine.eval(reader);
        } catch (Exception se) {
            se.printStackTrace();
        }
        return engine;
    }

    public void runUserScript(ScriptEngine engine, File script) {
        Reader reader;

        try {
            reader = new FileReader(script);
            engine.eval(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
