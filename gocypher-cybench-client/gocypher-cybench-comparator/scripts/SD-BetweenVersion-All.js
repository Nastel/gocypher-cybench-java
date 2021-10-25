var myReport = "reports/report-1635189787321-807778.0724691927.cybench"; // report file path
var myToken = "ws_305a4eb4-cbda-44fb-ba58-e89a7820e115_query"; // CyBench query token

// get all benchmarks <fingerprint : name> from report
var myFingerprintsAndNames = new HashMap(getFingerprintsFromReport(myReport));
var myFingerprints = new ArrayList(myFingerprintsAndNames.keySet());

// fetch all previous benchmarks from those fingerprints
forEach.call(myFingerprints, function(fingerprint) {
	var benchmarkName = myFingerprintsAndNames.get(fingerprint);
	fetchBenchmarks(benchmarkName, fingerprint, myToken); 
});


// COMPARATOR CONFIGURABLES //
var currentVersion = "1.0.2";
var compareVersion = "1.0.1";
var range = "ALL";

var currentVersionScores;
var compareVersionScores;


forEach.call(myFingerprints, function(fingerprint) {
	currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
	compareVersionScores = getBenchmarksByVersion(fingerprint, compareVersion);
	var benchmarkName = myFingerprintsAndNames.get(fingerprint);
	
	if(currentVersionScores != null && compareVersionScores != null) {
		currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
		compareVersionScoreModes = new ArrayList(compareVersionScores.keySet());
		
		forEach.call(currentVersionScoreModes, function(mode) {
			if (compareVersionScoreModes.contains(mode)) {
				var SD = sdCompareBetweenVersions(currentVersionScores.get(mode), compareVersionScores.get(mode), range);
				print(benchmarkName + ":" + mode + " - Between version " + currentVersion + " and " + compareVersion + ", the new score is " + SD + " deviations from the mean.\n");
			}
		});
	}
});