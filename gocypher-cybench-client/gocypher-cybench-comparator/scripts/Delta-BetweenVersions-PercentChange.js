/*
 * Copyright (C) 2020-2021, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

// EXAMPLE ARGS PASSED VIA COMMAND LINE
// -F -S scripts/SD-BetweenVersions.js -T ws_0a1evpqm-scv3-g43c-h3x2-f0pqm79f2d39_query -R reports/ -s BETWEEN -v PREVIOUS -r ALL -m DELTA -t PERCENT_CHANGE -p 10 


// loop through the fingerprints in my report
forEach.call(myFingerprints, function (fingerprint) {
    var currentVersion = getCurrentVersion(fingerprint);
    var benchmarkName = fingerprintsToNames.get(fingerprint);
    var benchmarkedModes = getRecentlyBenchmarkedModes(fingerprint, currentVersion);

    // loop through the modes tested within the current version of the fingerprint (current version = version benchmarked with)
    forEach.call(benchmarkedModes, function (mode) {
        currentVersionScores = getBenchmarksByMode(fingerprint, currentVersion, mode);
        compareVersionScores = getBenchmarksByMode(fingerprint, compareVersion, mode);

        // check to make sure there are benchmarks to compare to
        if (compareVersionScores != null) {
            logComparison(benchmarkName, mode);
            var percentChange = compareScores(currentVersionScores, compareVersionScores);
            var pass = passAssertion(percentChange);
        }
    });
});
