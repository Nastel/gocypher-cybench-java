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

// prints all fetched benchmarks
print(getAllBenchmarks());