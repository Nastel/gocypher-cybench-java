var myReport = ""; // report file path
var myToken = ""; // CyBench query token

// get all benchmarks <fingerprint : name> from report
var myFingerprintsAndNames = new HashMap(getFingerprintsFromReport(myReport));
var myFingerprints = new ArrayList(myFingerprintsAndNames.keySet());

// fetch all previous benchmarks from those fingerprints
forEach.call(myFingerprints, function (fingerprint) {
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);
    fetchBenchmarks(benchmarkName, fingerprint, myToken);
});


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
            var deviationsFromMean = sdCompareWithinVersion(currentVersionScores.get(mode), range);
            var pass = passAssertionDeviation(deviationsFromMean, deviationsAllowed);
            print(benchmarkName + " : " + mode + " - Within version " + currentVersion + ", the new score is " + deviationsFromMean + " deviations from the mean of the compared scores");
            if (pass) {
                print("Passed test\n");
            } else {
                print("FAILED test\n");
            }
        });
    }
});
