package com.gocypher.benchmarks.runner.utils;

import com.gocypher.benchmarks.core.utils.SecurityUtils;

import java.util.HashMap;
import java.util.Map;

public class SecurityBuilder {
    private Map<String,String> mapOfHashedParts ;

    public SecurityBuilder(){
        mapOfHashedParts = new HashMap<>() ;
    }
    public void generateSecurityHashForClasses (Class<?> clazz){
        if (clazz != null) {
            String hash = SecurityUtils.computeClassHash(clazz);
            if (hash != null) {
                mapOfHashedParts.put(clazz.getName(), hash);
            }
        }
    }
    public void generateSecurityHashForReport (String report){
        String hash = SecurityUtils.computeStringHash(report) ;
        if (hash != null){
            mapOfHashedParts.put("report",hash) ;
        }
    }
    public Map<String, Object> buildSignatures (){
        Map<String,Object> map = new HashMap<>() ;
        map.putAll( this.mapOfHashedParts) ;
        return map ;
    }

    public Map<String, String> getMapOfHashedParts() {
        return mapOfHashedParts;
    }
}
