package com.gocypher.benchmarks.jvm.scores;

import com.gocypher.benchmarks.core.model.BaseScoreConverter;
import com.gocypher.benchmarks.core.utils.IOUtils;

import java.util.Map;

public class IOReadWriteScoreConverter extends BaseScoreConverter {
    @Override
    public Double convertScore(Double score, Map<String,Object> metaData) {
        if (score != null){
            Double oldScore = new Double((double)score.doubleValue()/1000) ;
            long mb = 1_048_576 ;
            double benchmarkFileSize = (double)IOUtils.getHugeRandomBinaryFileSizeInBytes()/mb ;
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
