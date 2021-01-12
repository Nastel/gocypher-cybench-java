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

package com.gocypher.cybench.launcher.report;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

import org.openjdk.jmh.results.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gocypher.cybench.core.utils.SecurityUtils;
import com.gocypher.cybench.launcher.model.BenchmarkOverviewReport;
import com.gocypher.cybench.launcher.model.BenchmarkReport;
import com.gocypher.cybench.launcher.model.SecuredReport;
import com.gocypher.cybench.launcher.utils.ComputationUtils;
import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;
import com.jcabi.manifests.Manifests;

public class ReportingService {
	private static final Logger LOG = LoggerFactory.getLogger(ReportingService.class);
	private static ReportingService instance;

	private ReportingService() {

	}

	public static ReportingService getInstance() {
		if (instance == null) {
			instance = new ReportingService();
		}
		return instance;
	}

	public BenchmarkOverviewReport createBenchmarkReport(Collection<RunResult> jmhResults,
			Map<String, Map<String, String>> defaultBenchmarksMetadata) {
		BenchmarkOverviewReport overviewReport = new BenchmarkOverviewReport();
		for (RunResult item : jmhResults) {
			BenchmarkReport report = new BenchmarkReport();
			if (item.getPrimaryResult() != null) {
				report.setScore(item.getPrimaryResult().getScore());
				report.setUnits(item.getPrimaryResult().getScoreUnit());
				if (item.getPrimaryResult().getStatistics() != null) {
					report.setN(item.getPrimaryResult().getStatistics().getN());
					report.setMeanScore(item.getPrimaryResult().getStatistics().getMean());
					report.setMaxScore(item.getPrimaryResult().getStatistics().getMax());
					report.setMinScore(item.getPrimaryResult().getStatistics().getMin());
					if (!Double.isNaN(item.getPrimaryResult().getStatistics().getStandardDeviation())) {
						report.setStdDevScore(item.getPrimaryResult().getStatistics().getStandardDeviation());
					}
				}

			}
			if (item.getParams() != null) {
				report.setName(item.getParams().getBenchmark());
				report.setMode(item.getParams().getMode().shortLabel());
				// System.out.println("id:"+item.getParams().id());
				// System.out.println("Mode"+item.getParams().getMode().longLabel());
			}

            Collection<String> paramsKeys = item.getParams().getParamsKeys();
			for (String key : paramsKeys) {
                String value = item.getParams().getParam(key);
                LOG.info("Collected params. Key: {}, Value: {}", key, value);
                report.addMetadata("param" + BenchmarkReport.camelCase(key), value);
            }


            report.setBenchForkCount(Objects.requireNonNull(item.getParams()).getForks());
			report.setBenchThreadCount(item.getParams().getThreads());
			report.setBenchWarmUpIteration(item.getParams().getWarmup().getCount());
			report.setBenchWarmUpSeconds((int) item.getParams().getWarmup().getTime().getTime());
			report.setBenchMeasurementIteration(item.getParams().getMeasurement().getCount());
			report.setBenchMeasurementSeconds((int) item.getParams().getMeasurement().getTime().getTime());

			report.setGcCalls(getScoreFromJMHSecondaryResult(item, "·gc.count"));
			report.setGcTime(getScoreFromJMHSecondaryResult(item, "·gc.time"));
			report.setGcAllocationRate(getScoreFromJMHSecondaryResult(item, "·gc.alloc.rate"));
			report.setGcAllocationRateNorm(getScoreFromJMHSecondaryResult(item, "·gc.alloc.rate.norm"));
			report.setGcChurnPsEdenSpace(getScoreFromJMHSecondaryResult(item, "·gc.churn.PS_Eden_Space"));
			report.setGcChurnPsEdenSpaceNorm(getScoreFromJMHSecondaryResult(item, "·gc.churn.PS_Eden_Space.norm"));
			report.setGcChurnPsSurvivorSpace(getScoreFromJMHSecondaryResult(item, "·gc.churn.PS_Survivor_Space"));
			report.setGcChurnPsSurvivorSpaceNorm(getScoreFromJMHSecondaryResult(item, "·gc.churn.PS_Survivor_Space.norm"));

			report.setThreadsAliveCount(getScoreFromJMHSecondaryResult(item, "·threads.alive"));
			report.setThreadsDaemonCount(getScoreFromJMHSecondaryResult(item, "·threads.daemon"));
			report.setThreadsStartedCount(getScoreFromJMHSecondaryResult(item, "·threads.started"));

			report.setThreadsSafePointSyncTime(getScoreFromJMHSecondaryResult(item, "·rt.safepointSyncTime"));
			report.setThreadsSafePointTime(getScoreFromJMHSecondaryResult(item, "·rt.safepointTime"));
			report.setThreadsSafePointsCount(getScoreFromJMHSecondaryResult(item, "·rt.safepoints"));

			report.setThreadsSyncContendedLockAttemptsCount(getScoreFromJMHSecondaryResult(item, "·rt.sync.contendedLockAttempts"));
			report.setThreadsSyncMonitorFatMonitorsCount(getScoreFromJMHSecondaryResult(item, "·rt.sync.fatMonitors"));
			report.setThreadsSyncMonitorFutileWakeupsCount(getScoreFromJMHSecondaryResult(item, "·rt.sync.futileWakeups"));
			report.setThreadsSyncMonitorDeflations(getScoreFromJMHSecondaryResult(item, "·rt.sync.monitorDeflations"));
			report.setThreadsSyncMonitorInflations(getScoreFromJMHSecondaryResult(item, "·rt.sync.monitorInflations"));
			report.setThreadsSyncNotificationsCount(getScoreFromJMHSecondaryResult(item, "·rt.sync.notifications"));

			report.setThreadsSyncParksCount(getScoreFromJMHSecondaryResult(item, "·rt.sync.parks"));

			report.setThreadsSafePointsInterval(getScoreFromJMHSecondaryResult(item, "·safepoints.interval"));
			report.setThreadsSafePointsPause(getScoreFromJMHSecondaryResult(item, "·safepoints.pause"));
			report.setThreadsSafePointsPauseAvg(getScoreFromJMHSecondaryResult(item, "·safepoints.pause.avg"));
			report.setThreadsSafePointsPauseCount(getScoreFromJMHSecondaryResult(item, "·safepoints.pause.count"));
			report.setThreadsSafePointsPauseTTSP(getScoreFromJMHSecondaryResult(item, "·safepoints.ttsp"));
			report.setThreadsSafePointsPauseTTSPAvg(getScoreFromJMHSecondaryResult(item, "·safepoints.ttsp.avg"));
			report.setThreadsSafePointsPauseTTSPCount(getScoreFromJMHSecondaryResult(item, "·safepoints.ttsp.count"));

			report.setPerformanceProcessCpuLoad(getScoreFromJMHSecondaryResult(item,"performanceProcessCpuLoad",item.getParams().getMeasurement().getCount()));
			report.setPerformanceSystemCpuLoad(getScoreFromJMHSecondaryResult(item,"performanceSystemCpuLoad",item.getParams().getMeasurement().getCount()));
			report.setPerformanceProcessHeapMemoryUsed(getScoreFromJMHSecondaryResult(item,"performanceProcessHeapMemoryUsed",item.getParams().getMeasurement().getCount()));
			report.setPerformanceProcessNonHeapMemoryUsed(getScoreFromJMHSecondaryResult(item,"performanceProcessNonHeapMemoryUsed",item.getParams().getMeasurement().getCount()));

			String manifestData = null;
			if (Manifests.exists(Constants.BENCHMARK_METADATA)) {
				manifestData = Manifests.read(Constants.BENCHMARK_METADATA);
			}
			Map<String, Map<String, String>> benchmarksMetadata = ComputationUtils.parseBenchmarkMetadata(manifestData);
			Map<String, String> benchProps;
			if (manifestData != null) {
				benchProps = prepareBenchmarkProperties(report.getReportClassName(), benchmarksMetadata);
			} else {
				benchProps = prepareBenchmarkProperties(report.getReportClassName(), defaultBenchmarksMetadata);
			}
			if (benchProps.get("benchCategory") != null) {
				report.setCategory(benchProps.get("benchCategory"));
			}
			if (benchProps.get("benchContext") != null) {
				report.setContext(benchProps.get("benchContext"));
			}
			if (benchProps.get("benchVersion") != null) {
				report.setVersion(benchProps.get("benchVersion"));
			}
			report.recalculateScoresToMatchNewUnits();
			overviewReport.addToBenchmarks(report);
		}

		overviewReport.setTimestamp(System.currentTimeMillis());
		overviewReport.setTimestampUTC(ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli());
		overviewReport.computeScores();

		return overviewReport;
	}

	public Map<String, String> prepareBenchmarkProperties(String className,
			Map<String, Map<String, String>> benchmarksMetadata) {
		Map<String, String> benchmarkProperties = new HashMap<>();
		try {
			if (benchmarksMetadata.get(className) != null) {
				if (benchmarksMetadata.get(className).get("category") != null) {
					benchmarkProperties.put("benchCategory", benchmarksMetadata.get(className).get("category"));
				}
				if (benchmarksMetadata.get(className).get("context") != null) {
					benchmarkProperties.put("benchContext", benchmarksMetadata.get(className).get("context"));
				}
				if (benchmarksMetadata.get(className).get("version") != null) {
					benchmarkProperties.put("benchVersion", benchmarksMetadata.get(className).get("version"));
				}
			} else {
				benchmarkProperties.put("benchCategory", "CUSTOM");
				benchmarkProperties.put("benchContext", "Custom");
				benchmarkProperties.put("benchVersion", "1.0.0");
			}
			return benchmarkProperties;
		} catch (Exception e) {
			benchmarkProperties.put("benchCategory", "CUSTOM");
			benchmarkProperties.put("benchContext", "Custom");
			benchmarkProperties.put("benchVersion", "1.0.0");
			LOG.error("Error on resolving benchmarks category, context and version: class={}", className, e);
		}
		return benchmarkProperties;
	}

	public Map<String, String> prepareBenchmarkSettings(String className,
			Map<String, Map<String, String>> benchmarksMetadata) {
		Map<String, String> benchmarkProperties = new HashMap<>();
		try {
			if (benchmarksMetadata.get(className) != null) {
				if (benchmarksMetadata.get(className).get("context") != null) {
					benchmarkProperties.put("benchContext", benchmarksMetadata.get(className).get("context"));
				}
				if (benchmarksMetadata.get(className).get("version") != null) {
					benchmarkProperties.put("benchVersion", benchmarksMetadata.get(className).get("version"));
				}
			} else {
				benchmarkProperties.put("benchContext", "Custom");
				benchmarkProperties.put("benchVersion", "1.0.0");
			}
			return benchmarkProperties;
		} catch (Exception e) {
			benchmarkProperties.put("benchContext", "Custom");
			benchmarkProperties.put("benchVersion", "1.0.0");
			LOG.error("Error on resolving category: class={}", className, e);
		}
		return benchmarkProperties;
	}

	protected String getVersion(String fullClassName) {
		try {
			Class<?> clazz = Class.forName(fullClassName);
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Manifest manifest = new Manifest(loader.getResourceAsStream("META-INF/MANIFEST.MF"));
			String benchmarkPackageString = clazz.getPackage().getName().replace(".", "-") + "-version";
			return manifest.getMainAttributes().getValue(benchmarkPackageString);
		} catch (Exception e) {
			LOG.info("Could not locate the benchmark version", e);
		}
		return null;
	}

	public String prepareReportForDelivery(SecurityBuilder securityBuilder, BenchmarkOverviewReport report) {
		try {
			LOG.info("Preparing report: encrypt and sign...");
			SecuredReport securedReport = createSecuredReport(report);
			securityBuilder.generateSecurityHashForReport(securedReport.getReport());
			securedReport.setSignatures(securityBuilder.buildSignatures());
			String plainReport = JSONUtils.marshalToJson(securedReport);
			return SecurityUtils.encryptReport(plainReport);
		} finally {
			LOG.info("Report prepared: encrypted and signed");
		}
	}

	private SecuredReport createSecuredReport(BenchmarkOverviewReport report) {
		SecuredReport securedReport = new SecuredReport();
		securedReport.setReport(JSONUtils.marshalToJson(report));
		return securedReport;
	}

	private Double getScoreFromJMHSecondaryResult(RunResult result, String key) {
		if (result != null && result.getSecondaryResults() != null) {
			if (result.getSecondaryResults().get(key) != null) {
				return result.getSecondaryResults().get(key).getScore();
			}
		}
		return null;
	}
	private Double getScoreFromJMHSecondaryResult(RunResult result, String key, int denominator) {
		Double value = getScoreFromJMHSecondaryResult(result, key) ;
		if (value != null && denominator != 0){
			return value/denominator ;
		}
		return value ;
	}

}
