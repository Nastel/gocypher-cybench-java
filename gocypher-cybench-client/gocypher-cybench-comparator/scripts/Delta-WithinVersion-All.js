var currentVersionScores;

forEach.call(myFingerprints, function (fingerprint) {
    // get all benchmarks recorded for specified version (possible returns null!)
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null) {
        // loop through each benchmarked mode within this version
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());

        forEach.call(currentVersionScoreModes, function (mode) {
			logComparison(logConfigs, benchmarkName, mode);
            var delta = deltaCompareWithinVersion(currentVersionScores.get(mode), threshold, range);
            var pass = passAssertionPositive(delta);
        });
    }
});
