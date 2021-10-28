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

import com.gocypher.cybench.services.Requests;

public class ComparatorScriptEngine {

	private static final Logger log = LoggerFactory.getLogger(ComparatorScriptEngine.class);
    private final String[] engineDefs = { "var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');",
            "var Requests = Java.type('com.gocypher.cybench.services.Requests');",
            "var forEach = Array.prototype.forEach;", "var HashMap = Java.type('java.util.HashMap');",
            "var ArrayList = Java.type('java.util.ArrayList');" };
    private String currentVersion, previousVersion;
    private HashMap<String, String> myFingerprintsAndNames;
    private ArrayList<String> myFingerprints;
    private Map<String, Object> passedProps;

    public ComparatorScriptEngine(Map<String, String> passedProps) {
    	String token = passedProps.get("token");
    	String report = passedProps.get("report");
    	String method = passedProps.get("method");
    	String range = passedProps.get("range");
    	String scope = passedProps.get("scope");
    	String threshold = passedProps.get("threshold");
    	String percentChangeAllowed = passedProps.get("percentChangeAllowed");
    	String deviationsAllowed = passedProps.get("deviationsAllowed");
    	initiateFetch(token, report);
    	handleComparatorConfigs(method, range, scope, threshold, percentChangeAllowed, deviationsAllowed);
    }
    
    private void initiateFetch(String token, String report) {
    	if (token == null) {
    		log.warn("No access token provided!");
    		token = ConfigHandling.DEFAULT_TOKEN;
    	}
    	
    	if (report == null) {
    		log.warn("No report location provided, using default: {}", ConfigHandling.DEFAULT_REPORTS_LOCATION);
    		report = ConfigHandling.DEFAULT_REPORTS_LOCATION;
    	}
    	myFingerprintsAndNames = Requests.getFingerprintsFromReport(token, report);
        myFingerprints = new ArrayList<>(myFingerprintsAndNames.keySet());
        currentVersion = Requests.getCurrentVersion();
        previousVersion = Requests.getPreviousVersion();
        log.info("Found current version: {}", currentVersion);
        if (!previousVersion.equals(currentVersion))
        	log.info("Found previous version: {}", previousVersion);
    }
    
    @SuppressWarnings("unchecked")
	private void handleComparatorConfigs(String method, String range, String scope, String threshold, String percentChangeAllowed, String deviationsAllowed) {
    	Map<String, Object> comparatorProps = new HashMap<String, Object>();
    	comparatorProps.put(ConfigHandling.DEFAULT_IDENTIFIER_HEADER, ConfigHandling.loadDefaults());
    	
    	passedProps = new HashMap<String, Object>();
    	passedProps.put("currentVersion", currentVersion);
    	
    	if (StringUtils.isNotEmpty(scope)) {
    		passedProps.put("scope", scope);
	    	if (scope.toUpperCase().equals("BETWEEN")) {
	    		passedProps.put("compareVersion", previousVersion);
	    	}
    	}
    	if (StringUtils.isNotEmpty(method))
    		passedProps.put("method", method);
    	if (StringUtils.isNotEmpty(range))
    		passedProps.put("range", range);
    	if (StringUtils.isNotEmpty(threshold))
    		passedProps.put("threshold", threshold);
    	if (StringUtils.isNotEmpty(percentChangeAllowed))
    		passedProps.put("percentChangeAllowed", percentChangeAllowed);
    	if (StringUtils.isNotEmpty(deviationsAllowed))
    		passedProps.put("deviationsAllowed", deviationsAllowed);
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
            
            engine.put("myFingerprintsAndNames", myFingerprintsAndNames);
            engine.put("myFingerprints", myFingerprints);
            engine.put("logConfigs", passedProps);
            engine.put("method", passedProps.get("method"));
            engine.put("scope", passedProps.get("scope"));
            engine.put("range", passedProps.get("range"));
            engine.put("threshold", passedProps.get("threshold"));
            engine.put("percentChangeAllowed", passedProps.get("percentChangeAllowed"));
            engine.put("deviationsAllowed", passedProps.get("deviationsAllowed"));
            engine.put("currentVersion", currentVersion);
            engine.put("previousVersion", previousVersion);
            
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
