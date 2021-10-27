package com.gocypher.cybench.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.services.Requests;

public class ComparatorScriptEngine {

	private static final Logger log = LoggerFactory.getLogger(ComparatorScriptEngine.class);
    private final String[] engineDefs = { "var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');",
            "var Requests = Java.type('com.gocypher.cybench.services.Requests');",
            "var forEach = Array.prototype.forEach;", "var HashMap = Java.type('java.util.HashMap');",
            "var ArrayList = Java.type('java.util.ArrayList');" };
    private String token, report, range, percentChangeAllowed, deviationsAllowed;
    private Comparisons.Scope scope;
    private Comparisons.Threshold threshold;

    public ComparatorScriptEngine(String token, String report, String range, String scope, String threshold, String percentChangeAllowed, String deviationsAllowed) {
    	// TODO temp config handling, should reuse the ConfigHandling.checkConfigValidity
    	if (token == null) {
    		log.warn("No access token provided!");
    		token = ConfigHandling.DEFAULT_TOKEN;
    	}
    	this.token = token;
    	
    	if (report == null) {
    		log.warn("No report location provided, using default: {}", ConfigHandling.DEFAULT_REPORTS_LOCATION);
    		report = ConfigHandling.DEFAULT_REPORTS_LOCATION;
    	}
    	this.report = report;
    	
    	if (range == null) {
    		log.warn("No range provided, using default: {}", ConfigHandling.DEFAULT_COMPARE_RANGE);
    		range = ConfigHandling.DEFAULT_COMPARE_RANGE;
    	} else if (!range.toUpperCase().equals("ALL")) {
    		try {
    			Integer.parseInt(range);
    		} catch (Exception e) {
    			log.warn("{} is not a valid range, using default: {}", range, ConfigHandling.DEFAULT_COMPARE_RANGE);
    			range = ConfigHandling.DEFAULT_COMPARE_RANGE;
    		}
    	}
    	this.range = range;
    	
    	if (scope == null) {
    		log.warn("No scope provided, using default: {}", ConfigHandling.DEFAULT_COMPARE_SCOPE);
    		scope = ConfigHandling.DEFAULT_COMPARE_SCOPE.toString();
    	} else if (!EnumUtils.isValidEnum(Comparisons.Scope.class, scope.toUpperCase())) {
    		log.warn("{} is not a valid scope, using default: {}", scope, ConfigHandling.DEFAULT_COMPARE_SCOPE);
    		this.scope = ConfigHandling.DEFAULT_COMPARE_SCOPE;
    	}
    	this.scope = Comparisons.Scope.valueOf(scope.toUpperCase());
    	
    	if (threshold == null) {
    		log.warn("No threshold provided, using default: {}", ConfigHandling.DEFAULT_COMPARE_THRESHOLD);
    		threshold = ConfigHandling.DEFAULT_COMPARE_THRESHOLD.toString();
    	} else if (!EnumUtils.isValidEnum(Comparisons.Threshold.class, threshold.toUpperCase())) {
    		log.warn("{} is not a valid scope, using default: {}", threshold, ConfigHandling.DEFAULT_COMPARE_THRESHOLD);
    		this.threshold = ConfigHandling.DEFAULT_COMPARE_THRESHOLD;
    	}
    	this.threshold = Comparisons.Threshold.valueOf(threshold.toUpperCase());
    	
    	if (percentChangeAllowed == null) {
    		log.warn("No percent change allowed provided, using default: {}", ConfigHandling.DEFAULT_PERCENTAGE_ALLOWED);
    		percentChangeAllowed = ConfigHandling.DEFAULT_PERCENTAGE_ALLOWED.toString();
    	} else {
    		try {
    			Double.parseDouble(percentChangeAllowed);
    		} catch (Exception e) {
    			log.warn("{} is not a valid percentage, using default: {}", percentChangeAllowed, ConfigHandling.DEFAULT_PERCENTAGE_ALLOWED);
    			percentChangeAllowed = ConfigHandling.DEFAULT_PERCENTAGE_ALLOWED.toString();
    		}
    	}
    	this.percentChangeAllowed = percentChangeAllowed;
    	
    	if (deviationsAllowed == null) {
    		log.warn("No deviations allowed provided, using default: {}", ConfigHandling.DEFAULT_DEVIATIONS_ALLOWED);
    		deviationsAllowed = ConfigHandling.DEFAULT_DEVIATIONS_ALLOWED.toString();
    	} else {
    		try {
    			Double.parseDouble(deviationsAllowed);
    		} catch (Exception e) {
    			log.warn("{} is not a valid number for deviations allowed, using default: {}", deviationsAllowed, ConfigHandling.DEFAULT_DEVIATIONS_ALLOWED);
    			deviationsAllowed = ConfigHandling.DEFAULT_DEVIATIONS_ALLOWED.toString();
    		}
    	}
    	this.deviationsAllowed = deviationsAllowed;
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
            
            engine.put("range", range);
            engine.put("threshold", threshold);
            engine.put("percentChangeAllowed", percentChangeAllowed);
            engine.put("deviationsAllowed", deviationsAllowed);
            HashMap<String, String> myFingerprintsAndNames = Requests.getFingerprintsFromReport(report, token);
            ArrayList<String> myFingerprints = new ArrayList<>(myFingerprintsAndNames.keySet());
            engine.put("myFingerprintsAndNames", myFingerprintsAndNames);
            engine.put("myFingerprints", myFingerprints);
            engine.put("currentVersion", Requests.getCurrentVersion());
            if (scope.equals(Comparisons.Scope.BETWEEN))
            	engine.put("previousVersion", Requests.getPreviousVersion());
            
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
