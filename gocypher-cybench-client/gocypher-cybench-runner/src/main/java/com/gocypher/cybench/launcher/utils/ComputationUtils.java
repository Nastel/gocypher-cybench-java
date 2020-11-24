/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.gocypher.cybench.launcher.utils;

import com.gocypher.cybench.launcher.model.BenchmarkReport;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public static Map<String, Map<String, String>> parseBenchmarkMetadata(String configuration) {
        Map<String, Map<String, String>> benchConfiguration = new HashMap<>();
        if (configuration != null) {
            for (String item : configuration.split(";")) {
                String[] testCfg = item.split("=");
                if (testCfg != null && testCfg.length == 2) {
                    String name = testCfg[0];
                    if (benchConfiguration.get(name) == null) {
                        benchConfiguration.put(name, new HashMap<>());
                    }
                    String value = testCfg[1];
                    for (String cfgItem : value.split(",")) {
                        String[] values = cfgItem.split(":");
                        if (values != null && values.length == 2) {
                            String key = values[0];
                            String val = values[1];
                            benchConfiguration.get(name).put(key, val);
                        }
                    }
                }
            }
        }
        return benchConfiguration;
    }

    public static String formatInterval(final long l) {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }
}
