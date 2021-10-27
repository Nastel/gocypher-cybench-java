var currentVersionScores;

forEach.call(myFingerprints, function (fingerprint) {
    // get all benchmarks recorded for specified version (possible returns null!)
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null) {
        // loop through each benchmarked mode within this version
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());

        forEach.call(currentVersionScoreModes, function (mode) {
            var deviationsFromMean = sdCompareWithinVersionWithLogging(currentVersionScores.get(mode), range, benchmarkName, mode, currentVersion);
            var pass = passAssertionDeviation(deviationsFromMean, deviationsAllowed);
        });
    }
});
