var myReport = ""; // report file path OR report folder path
var myToken = ""; // CyBench query token

// get all benchmarks <fingerprint : name> from report
var myFingerprintsAndNames = new HashMap(getFingerprintsFromReport(myReport, myToken));
var myFingerprints = new ArrayList(myFingerprintsAndNames.keySet());


// COMPARATOR CONFIGURABLES //
var currentVersion = getCurrentVersion();
var range = "ALL";
var deviationsAllowed = 3;

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
