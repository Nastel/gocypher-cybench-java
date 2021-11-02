// EXAMPLE ARGS PASSED VIA COMMAND LINE
// -F -S scripts/SD-BetweenVersions.js -T ws_0a1evpqm-scv3-g43c-h3x2-f0pqm79f2d39_query -R reports/ -s WITHIN -r ALL -m SD -d 2


// loop through the fingerprints in my report
forEach.call(myFingerprints, function (fingerprint) {
    var currentVersion = getCurrentVersion(fingerprint);
    var benchmarkName = fingerprintsToNames.get(fingerprint);
    var benchmarkedModes = new ArrayList(myBenchmarks.get(fingerprint).get(currentVersion).keySet());

    // loop through the modes tested within the current version of the fingerprint (current version = version benchmarked with)
    forEach.call(benchmarkedModes, function (mode) {
        currentVersionScores = getBenchmarksByMode(fingerprint, currentVersion, mode);

        logComparison(logConfigs, benchmarkName, mode);
        var deviationsFromMean = compareScores(currentVersionScores);
        var pass = passAssertion(deviationsFromMean);
    });
});
