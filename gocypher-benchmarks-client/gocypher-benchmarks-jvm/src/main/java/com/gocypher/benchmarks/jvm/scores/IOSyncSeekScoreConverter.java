package com.gocypher.benchmarks.jvm.scores;

import com.gocypher.benchmarks.core.model.BaseScoreConverter;

import java.util.Map;

public class IOSyncSeekScoreConverter extends BaseScoreConverter {
    @Override
    public Double convertScore(Double score, Map<String,Object> metaData) {
        if (score != null){
            double nominatorInMB = 128 ;
            Double oldScore = new Double((double)score.doubleValue()/1000) ;
            return nominatorInMB/oldScore ;
        }
        return score;
    }

    @Override
    public String getUnits() {
        return "MB/s";
    }
}
