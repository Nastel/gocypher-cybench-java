var currentVersionScores;
var compareVersionScores;

forEach.call(myFingerprints, function (fingerprint) {
	var currentVersion = getCurrentVersion(fingerprint);
    // get all benchmarks recorded for specified version (possible returns null!)
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    compareVersionScores = getBenchmarksByVersion(fingerprint, compareVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null && compareVersionScores != null) {
        // loop through each benchmarked mode within this version
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
        compareVersionScoreModes = new ArrayList(compareVersionScores.keySet());

        forEach.call(currentVersionScoreModes, function (mode) {
            if (compareVersionScoreModes.contains(mode)) {
                logComparison(logConfigs, benchmarkName, mode);
                var percentChange = compareDelta(threshold, range, currentVersionScores.get(mode), compareVersionScores.get(mode));
                var pass = passAssertionPercentage(percentChange, percentChangeAllowed);
            }
        });
    }
});

