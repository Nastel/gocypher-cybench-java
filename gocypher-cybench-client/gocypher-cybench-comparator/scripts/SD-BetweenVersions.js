var currentVersionScores;
var previousVersionScores;

forEach.call(myFingerprints, function (fingerprint) {
	// get all benchmarks recorded for specified version (possible returns null!)
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    previousVersionScores = getBenchmarksByVersion(fingerprint, previousVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null && previousVersionScores != null) {
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
        compareVersionScoreModes = new ArrayList(previousVersionScores.keySet());

        forEach.call(currentVersionScoreModes, function (mode) {
            if (compareVersionScoreModes.contains(mode)) {
				logComparison(logConfigs, benchmarkName, mode);
                var deviationsFromMean = compareSD(range, currentVersionScores.get(mode), previousVersionScores.get(mode));
                var pass = passAssertionPositive(deviationsFromMean);
            }
        });
    }
});
