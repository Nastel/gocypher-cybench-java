package com.gocypher.benchmarks.core.model;

public abstract class BaseScoreConverter {
    public abstract Double convertScore (Double score) ;
    public abstract String getUnits () ;
}
