var myReport = ""; // report file path
var myToken = ""; // CyBench query token


// get all benchmark fingerprints from report
var myFingerprints = getFingerprintsFromReport(myReport);

// fetch all previous benchmarks from those fingerprints
forEach.call(myFingerprints, function(fingerprint) { fetchBenchmarks(fingerprint, myToken); });

// prints all fetched benchmarks
print(getAllBenchmarks());

