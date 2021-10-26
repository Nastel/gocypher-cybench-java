var myReport = ""; // report file path OR report folder path
var myToken = ""; // CyBench query token

// get all benchmarks <fingerprint : name> from report
var myFingerprintsAndNames = new HashMap(getFingerprintsFromReport(myReport, myToken));
var myFingerprints = new ArrayList(myFingerprintsAndNames.keySet());


// COMPARATOR CONFIGURABLES //
var currentVersion = getCurrentVersion();
var threshold = Comparisons.Threshold.GREATER;
var range = "ALL";

var currentVersionScores;

forEach.call(myFingerprints, function (fingerprint) {
    // get all benchmarks recorded for specified version (possible returns null!)
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null) {
        // loop through each benchmarked mode within this version
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());

        forEach.call(currentVersionScoreModes, function (mode) {
            var delta = deltaCompareWithinVersionWithLogging(currentVersionScores.get(mode), threshold, range, benchmarkName, mode, currentVersion);
            var pass = passAssertionPositive(delta);
        });
    }
});
