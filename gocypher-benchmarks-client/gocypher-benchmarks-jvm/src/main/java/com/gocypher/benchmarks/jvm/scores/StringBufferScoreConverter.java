package com.gocypher.benchmarks.jvm.scores;

import com.gocypher.benchmarks.core.model.BaseScoreConverter;

import java.util.Map;

public class StringBufferScoreConverter extends BaseScoreConverter {
    @Override
    public Double convertScore(Double score, Map<String,Object> metaData) {
        if (score != null){
            /*Double oldScore = new Double((double)score.doubleValue()/1000) ;
            Double newScore = (StringBenchmarks.numberOfIterations/oldScore) ;
            */
            Double newScore = score/1_000 ;
            return newScore ;
        }
        return null ;
    }

    @Override
    public String getUnits() {
        return "K Ops/s";
    }
}
