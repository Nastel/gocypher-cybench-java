package com.gocypher.benchmarks.runner.utils;

import com.gocypher.benchmarks.runner.model.BenchmarkReport;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class ComputationUtils {
    private static final int SCALE = 18;

    public static BigDecimal log10(BigDecimal b) {
        final int NUM_OF_DIGITS = SCALE + 2;
        // need to add one to get the right number of dp
        //  and then add one again to get the next number
        //  so I can round it correctly.

        MathContext mc = new MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN);
        //special conditions:
        // log(-x) -> exception
        // log(1) == 0 exactly;
        // log of a number lessthan one = -log(1/x)
        if (b.signum() <= 0) {
            throw new ArithmeticException("log of a negative number! (or zero)");
        } else if (b.compareTo(BigDecimal.ONE) == 0) {
            return BigDecimal.ZERO;
        } else if (b.compareTo(BigDecimal.ONE) < 0) {
            return (log10((BigDecimal.ONE).divide(b, mc))).negate();
        }

        StringBuilder sb = new StringBuilder();
        //number of digits on the left of the decimal point
        int leftDigits = b.precision() - b.scale();

        //so, the first digits of the log10 are:
        sb.append(leftDigits - 1).append(".");

        //this is the algorithm outlined in the webpage
        int n = 0;
        while (n < NUM_OF_DIGITS) {
            b = (b.movePointLeft(leftDigits - 1)).pow(10, mc);
            leftDigits = b.precision() - b.scale();
            sb.append(leftDigits - 1);
            n++;
        }

        BigDecimal ans = new BigDecimal(sb.toString());

        //Round the number to the correct number of decimal places.
        ans = ans.round(new MathContext(ans.precision() - ans.scale() + SCALE, RoundingMode.HALF_EVEN));
        return ans;
    }
    public static Double computeCategoryScore (List<BenchmarkReport> categoryReports){
        int magicNumberOfClusterSize = 7 ;
        int magicNumberScoreDecimalDenominator = 100 ;
        Double score = 0.0 ;
        int n = categoryReports.size() ;
        int countOfClusters = 0 ;

        if (n <= magicNumberOfClusterSize){
            countOfClusters = 1 ;
        }
        else {
            countOfClusters = (int)Math.ceil((double)n/magicNumberOfClusterSize) ;
        }
        for (int k = 0;k<countOfClusters;k++){
            Double productOfClusterScores = 1.0 ;
            for (int i = k*magicNumberOfClusterSize;i<k*magicNumberOfClusterSize+magicNumberOfClusterSize;i++){
                if (i < n) {
                    if (categoryReports.get(i) != null && categoryReports.get(i).getScore() != null) {
                        Double localScore = categoryReports.get(i).getScore() / magicNumberScoreDecimalDenominator;
                        productOfClusterScores *= localScore;
                    }
                }
            }
            score += Math.sqrt(productOfClusterScores) ;
        }
        return score ;
    }
}
