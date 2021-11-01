// loop through the fingerprints in my report
forEach.call(myFingerprints, function (fingerprint) {
	var currentVersion = getCurrentVersion(fingerprint);
	var benchmarkName = fingerprintsToNames.get(fingerprint);
	var benchmarkedModes = new ArrayList(myBenchmarks.get(fingerprint).get(currentVersion).keySet());

	// loop through the modes tested within the current version of the fingerprint (current version = version benchmarked with)
	forEach.call(benchmarkedModes, function (mode) {
		currentVersionScores = getBenchmarksByMode(fingerprint, currentVersion, mode);
		
		logComparison(logConfigs, benchmarkName, mode);
		var deviationsFromMean = compareSD(range, currentVersionScores);
		var pass = passAssertionDeviation(deviationsFromMean, deviationsAllowed);
	});
});
