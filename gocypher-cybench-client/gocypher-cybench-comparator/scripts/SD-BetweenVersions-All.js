var myReport = ""; // report file path OR report folder path
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
var previousVersion = getPreviousVersion();
var range = "ALL";

var currentVersionScores;
var previousVersionScores;


forEach.call(myFingerprints, function (fingerprint) {
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    previousVersionScores = getBenchmarksByVersion(fingerprint, previousVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);
	
	if (currentVersionScores == null)
		print(benchmarkName + " does not have any benchmarks within version " + currentVersion);
	if (previousVersionScores == null)
		print(benchmarkName + " does not have any benchmarks within version " + previousVersion);

    if (currentVersionScores != null && previousVersionScores != null) {
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
        compareVersionScoreModes = new ArrayList(previousVersionScores.keySet());

        forEach.call(currentVersionScoreModes, function (mode) {
            if (compareVersionScoreModes.contains(mode)) {
                var SD = sdCompareBetweenVersions(currentVersionScores.get(mode), previousVersionScores.get(mode), range);
                var pass = passAssertionPositive(SD);
                print(benchmarkName + ":" + mode + " - Between version " + currentVersion + " and " + previousVersion + ", the new score is " + SD + " deviations from the mean.");
                if (pass) {
                    print("Passed test");
                } else {
                    print("FAILED test");
                }
            }
        });
    }
});
