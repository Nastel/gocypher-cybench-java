package com.gocypher.benchmarks.core.model;

import java.util.Map;

public abstract class BaseScoreConverter {
    public abstract Double convertScore (Double score, Map<String,Object>metaData) ;
    public abstract String getUnits () ;
}
