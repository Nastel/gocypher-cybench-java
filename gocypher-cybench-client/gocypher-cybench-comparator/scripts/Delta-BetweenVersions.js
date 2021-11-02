// EXAMPLE ARGS PASSED VIA COMMAND LINE
// -F -S scripts/SD-BetweenVersions.js -T ws_0a1evpqm-scv3-g43c-h3x2-f0pqm79f2d39_query -R reports/ -s BETWEEN -v PREVIOUS -r ALL -m DELTA -t GREATER 


// loop through the fingerprints in my report
forEach.call(myFingerprints, function (fingerprint) {
    var currentVersion = getCurrentVersion(fingerprint);
    var benchmarkName = fingerprintsToNames.get(fingerprint);
    var benchmarkedModes = new ArrayList(myBenchmarks.get(fingerprint).get(currentVersion).keySet());

    // loop through the modes tested within the current version of the fingerprint (current version = version benchmarked with)
    forEach.call(benchmarkedModes, function (mode) {
        currentVersionScores = getBenchmarksByMode(fingerprint, currentVersion, mode);
        compareVersionScores = getBenchmarksByMode(fingerprint, compareVersion, mode);

        // check to make sure there are benchmarks to compare to
        if (compareVersionScores != null) {
            logComparison(logConfigs, benchmarkName, mode);
            var delta = compareScores(currentVersionScores, compareVersionScores);
            var pass = passAssertion(delta);
        }
    });
});

