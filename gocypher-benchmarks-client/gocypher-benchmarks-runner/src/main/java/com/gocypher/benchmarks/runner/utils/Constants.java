package com.gocypher.benchmarks.runner.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	public static final String SHOULD_SEND_REPORT_TO_JKOOL = "sendReport";
	public static final String URL_LINK_TO_GOCYPHER_REPORT = "reportUrl";
	public static final String BENCHMARK_REPORT_NAME = "reportName";
	public static final String BENCHMARK_RUN_CLASSES = "benchmarkClasses";

	public static final Map<String,String> BENCHMARKS_SCORES_COMPUTATIONS_MAPPING = new HashMap<>() ;
	static {
		BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put("com.gocypher.benchmarks.jvm.client.tests.IOBenchmarks.seekAndReadFileHugeChunks","com.gocypher.benchmarks.jvm.scores.IOSeekScoreConverter") ;
		BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put("com.gocypher.benchmarks.jvm.client.tests.IOBenchmarks.seekAndReadFileSmallChunks","com.gocypher.benchmarks.jvm.scores.IOSeekScoreConverter") ;
		BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put("com.gocypher.benchmarks.jvm.client.tests.IOBenchmarks.seekAndWriteFileSmallChunks","com.gocypher.benchmarks.jvm.scores.IOSeekScoreConverter") ;
		BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put("com.gocypher.benchmarks.jvm.client.tests.IOBenchmarks.seekAndWriteFileHugeChunks","com.gocypher.benchmarks.jvm.scores.IOSeekScoreConverter") ;
		BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put("com.gocypher.benchmarks.jvm.client.tests.IOBenchmarks.rwFileUsingFileStreamsSmallChunks","com.gocypher.benchmarks.jvm.scores.IOReadWriteScoreConverter") ;
		BENCHMARKS_SCORES_COMPUTATIONS_MAPPING.put("com.gocypher.benchmarks.jvm.client.tests.IOBenchmarks.rwFileUsingFileStreamsHugeChunks","com.gocypher.benchmarks.jvm.scores.IOReadWriteScoreConverter") ;

	}

}
