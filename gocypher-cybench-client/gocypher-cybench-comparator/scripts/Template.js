/*
 * Copyright (C) 2020-2022, K2N.IO.
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
// -F -S scripts/Template.js -T ws_0a1evpqm-scv3-g43c-h3x2-f0pqm79f2d39_query -R reports/ -s WITHIN -r ALL -m DELTA -t GREATER 


// loop through the my benchmarks
forEach.call(myBenchmarks, function (benchmark) {
    // var benchmark represents a ComparedBenchmark Model

    // returns a list of benchmarks previously tested (with the same fingerprint and mode)
    benchmarksToCompareAgainst = getBenchmarksToCompareAgainst(benchmark);

    // represents an ENUM (PASS, FAIL, SKIP) - SKIP means this benchmark was not previously tested
    compareState = runComparison(benchmark, benchmarksToCompareAgainst);

    // after running a comparison, benchmark object will have contain properties that represent comparison statistics
    comparedAgainstMean = benchmark.getCompareMean();
    comapredAgainstStandardDeviation = benchmark.getCompareSD();

    score = benchmark.getScore();
    delta = benchmark.getDelta();
    percentChange = benchmark.getPercentChange();
    deviationsFromMean = benchmark.getDeviationsFromMean();
});
