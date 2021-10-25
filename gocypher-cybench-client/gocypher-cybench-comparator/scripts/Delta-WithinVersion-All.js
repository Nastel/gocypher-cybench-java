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
var threshold = Comparisons.Threshold.GREATER;
var range = "ALL";

var currentVersionScores;

forEach.call(myFingerprints, function(fingerprint) {
	// get all benchmarks recorded for specified version (possible returns null!)
	currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
	var benchmarkName = myFingerprintsAndNames.get(fingerprint);

	if(currentVersionScores != null) {
		// loop through each benchmarked mode within this version
		currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
		
		forEach.call(currentVersionScoreModes, function(mode) {
			var delta = deltaCompareWithinVersion(currentVersionScores.get(mode), threshold, range);
			print(benchmarkName + " : " + mode + " - Within version " + currentVersion + ", the delta value between the recent score and the average of the compared to scores is " + delta + "\n");
		});
	}
});
