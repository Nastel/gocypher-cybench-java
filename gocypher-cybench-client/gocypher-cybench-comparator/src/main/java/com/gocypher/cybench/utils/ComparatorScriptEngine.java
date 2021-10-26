package com.gocypher.cybench.utils;

import java.io.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ComparatorScriptEngine {

    private final String[] engineDefs = { "var Comparisons = Java.type('com.gocypher.cybench.utils.Comparisons');",
            "var Requests = Java.type('com.gocypher.cybench.services.Requests');",
            "var forEach = Array.prototype.forEach;", "var HashMap = Java.type('java.util.HashMap');",
            "var ArrayList = Java.type('java.util.ArrayList');" };

    public ComparatorScriptEngine() {
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
