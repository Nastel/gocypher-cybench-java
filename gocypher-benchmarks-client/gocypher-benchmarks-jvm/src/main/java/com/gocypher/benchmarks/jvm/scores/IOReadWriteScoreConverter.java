package com.gocypher.benchmarks.jvm.scores;

import com.gocypher.benchmarks.core.model.BaseScoreConverter;
import com.gocypher.benchmarks.core.utils.IOUtils;

public class IOReadWriteScoreConverter extends BaseScoreConverter {
    @Override
    public Double convertScore(Double score) {
        if (score != null){
            Double oldScore = new Double((double)score.doubleValue()/1000) ;
            long mb = 1048576 ;
            double benchmarkFileSize = (double)IOUtils.getRandomBinaryFileSizeInBytes()/mb ;
            double newScore = benchmarkFileSize/oldScore ;
            return new Double(newScore) ;
        }

        return score ;
    }

    @Override
    public String getUnits() {
        return "MB/s";
    }
}
