package com.gocypher.benchmarks.jvm.scores;

import com.gocypher.benchmarks.core.model.BaseScoreConverter;

public class IOSeekScoreConverter extends BaseScoreConverter {
    @Override
    public Double convertScore(Double score) {
        if (score != null){
            double oneGBinMB = 1_024 ;
            Double oldScore = new Double((double)score.doubleValue()/1000) ;
            return oneGBinMB/oldScore ;
        }
        return score;
    }

    @Override
    public String getUnits() {
        return "MB/s";
    }
}
