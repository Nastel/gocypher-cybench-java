package com.gocypher.benchmarks.runner.model;

import java.util.HashMap;
import java.util.Map;

public class SecuredReport {
    private Map<String, Object> signatures ;
    private String report ;

    public SecuredReport (){
        signatures = new HashMap<>() ;
    }

    public Map<String, Object> getSignatures() {
        return signatures;
    }

    public void setSignatures(Map<String, Object> signatures) {
        this.signatures = signatures;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
