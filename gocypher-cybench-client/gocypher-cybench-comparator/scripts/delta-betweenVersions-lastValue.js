var myReport = ""; // report file path
var myToken = ""; // CyBench query token

// get all benchmarks <fingerprint : name> from report
var myFingerprintsAndNames = new HashMap(getFingerprintsFromReport(myReport));
var myFingerprints = new ArrayList(myFingerprintsAndNames.keySet());

// fetch all previous benchmarks from those fingerprints
forEach.call(myFingerprints, function(fingerprint) {
	var benchmarkName = myFingerprintsAndNames.get(fingerprint);
	fetchBenchmarks(benchmarkName, fingerprint, myToken); 
});



// COMPARATOR CONFIGURABLES //
var currentVersion = "1.0.1";
var compareVersion = "1.0.0";
var threshold = Comparisons.Threshold.GREATER;
var range = 1;

var currentVersionScores;
var compareVersionScores;

forEach.call(myFingerprints, function(fingerprint) {
	// get all benchmarks recorded for specified version (possible returns null!)
	currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
	compareVersionScores = getBenchmarksByVersion(fingerprint, compareVersion);
	var benchmarkName = myFingerprintsAndNames.get(fingerprint);
	
	if(currentVersionScores != null && compareVersionScores != null) {
		// loop through each benchmarked mode within this version
		currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
		compareVersionScoreModes = new ArrayList(compareVersionScores.keySet());
		forEach.call(currentVersionScoreModes, function(mode) {
			if (compareVersionScoreModes.contains(mode)) {
				var delta = compareWithDelta(currentVersionScores.get(mode), compareVersionScores.get(mode), threshold, range);
				print(benchmarkName + ":" + mode + " - Between version " + currentVersion + " and " + compareVersion + ", the change in last value recorded was " + delta);
			}
		});
	}
});

