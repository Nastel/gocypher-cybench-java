forEach.call(myFingerprints, function (fingerprint) {
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    previousVersionScores = getBenchmarksByVersion(fingerprint, previousVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null && previousVersionScores != null) {
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
        compareVersionScoreModes = new ArrayList(previousVersionScores.keySet());

        forEach.call(currentVersionScoreModes, function (mode) {
            if (compareVersionScoreModes.contains(mode)) {
                var deviationsFromMean = sdCompareBetweenVersionsWithLogging(currentVersionScores.get(mode), previousVersionScores.get(mode), range, benchmarkName, mode, currentVersion, previousVersion);
                var pass = passAssertionPositive(deviationsFromMean);
            }
        });
    }
});
