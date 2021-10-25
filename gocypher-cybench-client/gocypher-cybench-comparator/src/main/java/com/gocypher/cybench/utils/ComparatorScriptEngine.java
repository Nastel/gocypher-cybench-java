package com.gocypher.cybench.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ComparatorScriptEngine {
	
	public ComparatorScriptEngine(){ }
	
	public File loadUserScript(String scriptPath) {
		File userScript = new File(scriptPath);
		return userScript;
	}
	
	public ScriptEngine prepareScriptEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("nashorn");
		try {
			engine.eval("var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');");
			engine.eval("var Requests = Java.type('com.gocypher.cybench.services.Requests');");
			engine.eval("var forEach = Array.prototype.forEach;");
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/ComparatorScriptBindings.js")));
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
