package com.gocypher.cybench.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ComparatorScriptEngine {
	
	public ComparatorScriptEngine(String scriptPath){
		loadUserScript(scriptPath);
	}
	
	public void loadUserScript(String scriptPath) {
		File script = new File(scriptPath);
		this.runUserScript(script);
	}
	
	private ScriptEngine prepareScriptEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("nashorn");
		try {
			
			engine.eval("var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/CompareScriptBindings.js")));
			engine.eval(reader);
			
		} catch (Exception se) {
			se.printStackTrace();
		}
		return engine;
	}
	
	private void runUserScript(File script) {
		ScriptEngine engine = this.prepareScriptEngine();
		
		Reader reader;
		try {
			reader = new FileReader(script);
			engine.eval(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
