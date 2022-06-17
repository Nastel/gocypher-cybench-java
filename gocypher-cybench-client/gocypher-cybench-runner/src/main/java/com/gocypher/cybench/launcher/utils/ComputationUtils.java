/*
 * Copyright (C) 2020-2022, K2N.IO.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package com.gocypher.cybench.launcher.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.gocypher.cybench.launcher.model.BenchmarkReport;

public final class ComputationUtils {
    private static final int SCALE = 18;

    private ComputationUtils() {
    }

    public static BigDecimal log10(BigDecimal b) {
        int NUM_OF_DIGITS = SCALE + 2;
        // need to add one to get the right number of dp
        // and then add one again to get the next number
        // so I can round it correctly.

        MathContext mc = new MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN);
        // special conditions:
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
        // number of digits on the left of the decimal point
        int leftDigits = b.precision() - b.scale();

        // so, the first digits of the log10 are:
        sb.append(leftDigits - 1).append(".");

        // this is the algorithm outlined in the webpage
        int n = 0;
        while (n < NUM_OF_DIGITS) {
            b = (b.movePointLeft(leftDigits - 1)).pow(10, mc);
            leftDigits = b.precision() - b.scale();
            sb.append(leftDigits - 1);
            n++;
        }

        BigDecimal ans = new BigDecimal(sb.toString());

        // Round the number to the correct number of decimal places.
        ans = ans.round(new MathContext(ans.precision() - ans.scale() + SCALE, RoundingMode.HALF_EVEN));
        return ans;
    }

    public static Double computeCategoryScore(List<BenchmarkReport> categoryReports) {
        int magicNumberOfClusterSize = 7;
        int magicNumberScoreDecimalDenominator = 100;
        double score = 0.0;
        int n = categoryReports.size();
        int countOfClusters;

        if (n <= magicNumberOfClusterSize) {
            countOfClusters = 1;
        } else {
            countOfClusters = (int) Math.ceil((double) n / magicNumberOfClusterSize);
        }
        for (int k = 0; k < countOfClusters; k++) {
            double productOfClusterScores = 1.0;
            for (int i = k * magicNumberOfClusterSize; i < k * magicNumberOfClusterSize
                    + magicNumberOfClusterSize; i++) {
                if (i < n) {
                    if (categoryReports.get(i) != null && categoryReports.get(i).getScore() != null) {
                        double localScore = categoryReports.get(i).getScore() / magicNumberScoreDecimalDenominator;
                        productOfClusterScores *= localScore;
                    }
                }
            }
            score += Math.sqrt(productOfClusterScores);
        }
        return score;
    }

    // com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAP
    // IComparisonBenchmarks=category:IO,context:JVM,version:1.0.0;
    public static Map<String, Map<String, String>> parseBenchmarkMetadata(String configuration) {
        Map<String, Map<String, String>> benchConfiguration = new HashMap<>();
        if (configuration != null) {
            for (String item : configuration.split(";")) {
                String[] testCfg = item.split("=");
                if (testCfg != null && testCfg.length == 2) {
                    String name = testCfg[0];
                    benchConfiguration.computeIfAbsent(name, k -> new HashMap<>());
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

    public static Map<String, Object> customUserDefinedProperties(String customPropertiesStr) {
        Map<String, Object> customUserProperties = new HashMap<>();
        if (StringUtils.isNotEmpty(customPropertiesStr)) {
            String[] pairs = customPropertiesStr.split(";");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    customUserProperties.put(kv[0], kv[1]);
                }
            }
        }

        return customUserProperties;
    }

    public static String formatInterval(long l) {
        long hr = TimeUnit.MILLISECONDS.toHours(l);
        long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        long ms = TimeUnit.MILLISECONDS.toMillis(
                l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }

    public static String createFileNameForReport(String reportName, long timestamp, BigDecimal totalScore,
            boolean isEncryptedFile) {
        if (StringUtils.isNotEmpty(reportName) && totalScore != null) {
            return reportName.replaceAll(" ", "_").toLowerCase() + "-" + timestamp + "-" + totalScore + (isEncryptedFile
                    ? Constants.CYB_ENCRYPTED_REPORT_FILE_EXTENSION : Constants.CYB_REPORT_FILE_EXTENSION);
        }
        return Constants.DEFAULT_REPORT_FILE_NAME_SUFFIX + "-" + timestamp + (isEncryptedFile
                ? Constants.CYB_ENCRYPTED_REPORT_FILE_EXTENSION : Constants.CYB_REPORT_FILE_EXTENSION);
    }

    public static String getRequestHeader(String token, String email) {
        if (StringUtils.isNotEmpty(email) && StringUtils.isNotEmpty(token)) {
            return token + ":" + email;
        } else if (StringUtils.isNotEmpty(token)) {
            return token;
        } else if (StringUtils.isNotEmpty(email)) {
            return ":" + email;
        } else {
            return "";
        }
    }
}
