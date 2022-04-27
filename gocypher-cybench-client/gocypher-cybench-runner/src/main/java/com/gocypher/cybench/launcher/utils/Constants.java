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

import java.util.HashMap;
import java.util.Map;

public class Constants {

    private static final String PROD_HOST = "https://app.cybench.io";
    private static final String LOCAL_HOST = "http://localhost:8080";
    public static final String APP_HOST = PROD_HOST;

    public static final String SEND_REPORT = "sendReport";
    public static final String REPORT_URL = "reportUrl";
    public static final String REPORT_USER_URL = "reportUserUrl";
    public static final String BENCHMARK_REPORT_NAME = "reportName";
    public static final String BENCHMARK_RUN_CLASSES = "benchmarkClasses";
    public static final String USER_PROPERTY_PREFIX = "user.";
    public static final String REPORT_SOURCE = "benchSource";
    public static final String REPORT_VERSION = "benchVersion";
    public static final String SEND_REPORT_URL = "sendReportToURL";
    public static final String INTELLIJ_PLUGIN = "intelliJPlugin";
    
    public static final String ALLOW_UPLOAD = "allowUpload";
    public static final String NUM_REPORTS_IN_REPO = "numReportsInRepo";
    public static final String REPORTS_ALLOWED_FROM_SUB = "numReportsPerMonth";    

    public static final String PROJECT_NAME = "artifactId";
    public static final String PROJECT_VERSION = "version";

    // --------------------------- Properties that configure the runner execution
    // ------------------------------------------
    public static final String NUMBER_OF_FORKS = "numberOfBenchmarkForks";
    public static final String MEASUREMENT_ITERATIONS = "measurementIterations";
    public static final String MEASUREMENT_SECONDS = "measurementSeconds";
    public static final String WARM_UP_ITERATIONS = "warmUpIterations";
    public static final String WARM_UP_SECONDS = "warmUpSeconds";
    public static final String RUN_THREAD_COUNT = "runThreadCount";
    public static final String BENCHMARK_MODES = "benchmarkModes";
    public static final String JMH_ARGUMENTS = "jmhArguments";

    public static final String REPORT_UPLOAD_STATUS = "reportUploadStatus";
    public static final String BENCHMARK_METADATA = "benchmarkMetadata";
    public static final String REPORT_PRIVATE = "private";
    public static final String REPORT_PUBLIC = "public";
    public static final String CYB_REPORT_JSON_FILE = "report";
    public static final String CYB_REPORT_CYB_FILE = "report.cyb";
    public static final String CYB_REPORT_FILE_EXTENSION = ".cybench";
    public static final String CYB_ENCRYPTED_REPORT_FILE_EXTENSION = ".cyb";
    public static final String APPEND_SCORE_TO_FNAME = "appendScore";
    public static final String COLLECT_HW = "collectHw";
    public static final String CYB_UPLOAD_URL = System.getProperty("cybench.manual.upload.url",
            APP_HOST + "/cybench/upload");
    public static final String DEFAULT_REPORT_FILE_NAME_SUFFIX = "report";
    public static final String USER_REPORT_TOKEN = "benchAccessToken";
    public static final String USER_QUERY_TOKEN = "benchQueryToken";
    public static final String USER_EMAIL_ADDRESS = "emailAddress";

    public static final String FOUND_TOKEN_REPOSITORIES = "reposFound";

    public static final Map<String, String> BENCHMARKS_SCORES_COMPUTATIONS_MAPPING = new HashMap<>();

    public static final String LAUNCHER_CONFIGURATION = "launcher_configuration";
    public static final String AUTOMATED_COMPARISON_CONFIGURATION = "automation_configuration";

    // --------------------------- Properties that configure the runner execution for Eclipse
    // ------------------------------------------
    public static final String COLLECT_HARDWARE_PROPS = "collectHardware";
    public static final String USE_CYBENCH_CONFIGURATION = "useCyBenchConfig";
    public static final String SELECTED_CLASS_PATHS = "selectedClassesToRun";

    // --------------------------- Properties that configure automatic comparison
    // ------------------------------------------
    public static final String AUTO_SHOULD_RUN_COMPARISON = "shouldRunAutoComparison";
    public static final String AUTO_SCOPE = "scope";
    public static final String AUTO_COMPARE_VERSION = "compareVersion";
    public static final String AUTO_LATEST_REPORTS = "numLatestReports";
    public static final String AUTO_ANOMALIES_ALLOWED = "anomaliesAllowed";
    public static final String AUTO_METHOD = "method";
    public static final String AUTO_THRESHOLD = "threshold";
    public static final String AUTO_PERCENT_CHANGE = "percentChangeAllowed";
    public static final String AUTO_DEVIATIONS_ALLOWED = "deviationsAllowed";
    // ---------------------------------------------------------------------------------------------------------------------------------
    public static String[] excludedFromReportArgument = { USER_REPORT_TOKEN, BENCHMARK_RUN_CLASSES,
            BENCHMARK_REPORT_NAME, COLLECT_HW, SEND_REPORT, REPORT_UPLOAD_STATUS, COLLECT_HARDWARE_PROPS,
            USE_CYBENCH_CONFIGURATION, SELECTED_CLASS_PATHS, USER_EMAIL_ADDRESS };

    static {

        // ---------------------------Score converters for sync file seek
        // access------------------------------------------
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncFileSeekBenchmarks.seekAndReadFileUsingSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncFileSeekBenchmarks.seekAndReadFileUsingHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncFileSeekBenchmarks.seekAndWriteFileUsingSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncFileSeekBenchmarks.seekAndWriteFileUsingHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncFileSeekBenchmarks.seekAndCopyFileUsingSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncFileSeekBenchmarks.seekAndCopyFileUsingHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSeekScoreConverter");

        // ---------------------------Score converters for sync file seek
        // access------------------------------------------

        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncFileSeekBenchmarks.seekAndReadFileUsingSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncFileSeekBenchmarks.seekAndReadFileUsingHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncFileSeekBenchmarks.seekAndWriteFileUsingSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncFileSeekBenchmarks.seekAndWriteFileUsingHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncFileSeekBenchmarks.seekAndCopyFileUsingSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncSeekScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncFileSeekBenchmarks.seekAndCopyFileUsingHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncSeekScoreConverter");

        // ----------------------Score converter for async file copy
        // benchmarks----------------------------------------------
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAPIComparisonBenchmarks.copyFileUsingMappedByteBuffer",
                "com.gocypher.cybench.launcher.scores.IOReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAPIComparisonBenchmarks.copyFileUsingFileStreamAndSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAPIComparisonBenchmarks.copyFileUsingFileStreamAndHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAPIComparisonBenchmarks.copyFileUsingBufferedStreamAndSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAPIComparisonBenchmarks.copyFileUsingBufferedStreamAndHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAPIComparisonBenchmarks.copyFileUsingDirectBufferedStreamAndSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOAsyncAPIComparisonBenchmarks.copyFileUsingDirectBufferedStreamAndHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOReadWriteScoreConverter");

        // ----------------------Score converter for sync file copy
        // benchmarks----------------------------------------------

        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncAPIComparisonBenchmarks.copyFileUsingMappedByteBuffer",
                "com.gocypher.cybench.launcher.scores.IOSyncReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncAPIComparisonBenchmarks.copyFileUsingFileStreamAndSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncAPIComparisonBenchmarks.copyFileUsingFileStreamAndHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncAPIComparisonBenchmarks.copyFileUsingBufferedStreamAndSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncAPIComparisonBenchmarks.copyFileUsingBufferedStreamAndHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncAPIComparisonBenchmarks.copyFileUsingDirectBufferedStreamAndSmallChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncReadWriteScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.IOSyncAPIComparisonBenchmarks.copyFileUsingDirectBufferedStreamAndHugeChunks",
                "com.gocypher.cybench.launcher.scores.IOSyncReadWriteScoreConverter");
        // ---------------------Score converter for String
        // operations--------------------------------------------------------------

        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.StringBenchmarks.stringConcatMultiChars",
                "com.gocypher.cybench.launcher.scores.StringBufferScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.StringBenchmarks.stringBufferConcatMultiChars",
                "com.gocypher.cybench.launcher.scores.StringBufferScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.StringBenchmarks.stringReplaceAll",
                "com.gocypher.cybench.launcher.scores.StringBufferScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.StringBenchmarks.stringBufferReplaceAll",
                "com.gocypher.cybench.launcher.scores.StringBufferScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.StringBenchmarks.findRegexCompiled",
                "com.gocypher.cybench.launcher.scores.StringBufferScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.StringBenchmarks.findRegexUnCompiled",
                "com.gocypher.cybench.launcher.scores.StringBufferScoreConverter");

        // ---------------------Score converter for Number
        // operations--------------------------------------------------------------
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.NumberBenchmarks.generateAndAddDoubleNumbers",
                "com.gocypher.cybench.launcher.scores.NumbersScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.NumberBenchmarks.generateAndAddAtomicNumbers",
                "com.gocypher.cybench.launcher.scores.NumbersScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.NumberBenchmarks.generateAndAddBigDecimalNumbers",
                "com.gocypher.cybench.launcher.scores.NumbersScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.NumberBenchmarks.generateAndLogarithmDoubleNumbers",
                "com.gocypher.cybench.launcher.scores.NumbersScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.NumberBenchmarks.generateAndPowerDoubleNumbers",
                "com.gocypher.cybench.launcher.scores.NumbersScoreConverter");

        // ---------------------Score converter for JSON
        // operations--------------------------------------------------------------
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gsonWithSmallJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gsonWithAverageJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gsonWithBigJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.moshiWithBigJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.moshiWithSmallJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.moshiWithAverageJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jsonIteratorWithBigJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jsonIteratorWithSmallJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jsonIteratorWithAverageJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.qsonWithBigJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.qsonWithSmallJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.qsonWithAverageJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gensonWithAverageJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gensonWithBigJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gensonWithSmallJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jacksonWithSmallJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jacksonWithAverageJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jacksonWithBigJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.boonWithSmallJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.boonWithAverageJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.boonWithBigJSON",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gsonWithSmallObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gsonWithAverageObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gsonWithBigObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jacksonWithSmallObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jacksonWithAverageObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jacksonWithBigObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.boonWithBigObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.boonWithSmallObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.boonWithAverageObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gensonWithBigObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gensonWithSmallObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.gensonWithAverageObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.moshiWithBigObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.moshiWithSmallObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.moshiWithAverageObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.qsonWithBigObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.qsonWithSmallObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.qsonWithAverageObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jsonIteratorWithBigObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jsonIteratorWithSmallObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.JsonLibraryBenchmark.jsonIteratorWithAverageObject",
                "com.gocypher.cybench.launcher.scores.JsonScoreConverter");

        // ---------------------Score converter for LIST
        // operations--------------------------------------------------------------
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.arrayListAdd",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put("com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.stackAdd",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.linkedListAdd",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.arrayListRemove",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.stackRemove",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.linkedListRemove",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.arrayListUpdate",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.stackUpdate",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");
        BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put(
                "com.gocypher.cybench.jmh.jvm.client.tests.ListsBenchmark.linkedListUpdate",
                "com.gocypher.cybench.launcher.scores.ListScoreConverter");

    }

}
