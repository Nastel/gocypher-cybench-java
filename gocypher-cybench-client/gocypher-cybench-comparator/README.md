# gocypher-cybench-comparator

This app is designed to compare recent CyBench reports in your project to previously ran CyBench benchmarks hosted on
the CyBench site.

Dependencies for your project:

* Maven:
    ```xml
    <repositories>
        <repository>
            <id>oss.sonatype.org</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    ...
    <dependency>
        <groupId>com.gocypher.cybench.client</groupId>
        <artifactId>gocypher-cybench-comparator</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
    ```

* Gradle:
    ```groovy
    runtime 'com.gocypher.cybench.client:gocypher-cybench-comparator:1.0.0-SNAPSHOT'
    ```

## Scripting

Template scripts located in the scripts folder [scripts](scripts/)

* The CyBench Comparator tool allows you to build your own customized .js files that hit benchmark fetch methods,
  comparison methods, and assertion methods.
    * This allows you more flexibility in what you do with your benchmark scores and comparison statistics.

### Example Template

* This is the [Delta-BetweenVersions-LastValue-PercentChange](scripts/Delta-BetweenVersions-LastValue-PercentChange.js)
  template

```js
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
var previousVersion = getPreviousVersion();
var threshold = Comparisons.Threshold.PERCENT_CHANGE;
var range = 1;
var percentChangeAllowed = 10;

var currentVersionScores;
var previousVersionScores;

forEach.call(myFingerprints, function (fingerprint) {
    // get all benchmarks recorded for specified version (possible returns null!)
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    previousVersionScores = getBenchmarksByVersion(fingerprint, previousVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null && previousVersionScores != null) {
        // loop through each benchmarked mode within this version
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
        compareVersionScoreModes = new ArrayList(previousVersionScores.keySet());
        forEach.call(currentVersionScoreModes, function (mode) {
            if (compareVersionScoreModes.contains(mode)) {
                var percentChange = deltaCompareBetweenVersions(currentVersionScores.get(mode), previousVersionScores.get(mode), threshold, range);
                var pass = passAssertionPercentage(percentChange, percentChangeAllowed);
                print(benchmarkName + " : " + mode + " - Between version " + currentVersion + " and " + previousVersion + ", the percent change in last value recorded was " + percentChange + "%");
                if (pass) {
                    print("Passed test\n");
                } else {
                    print("FAILED test\n");
                }
            }
        });
    }
});
```

#### Template Walkthrough

```js
// get all benchmarks <fingerprint : name> from report
var myFingerprintsAndNames = new HashMap(getFingerprintsFromReport(myReport));
var myFingerprints = new ArrayList(myFingerprintsAndNames.keySet());

// fetch all previous benchmarks from those fingerprints
forEach.call(myFingerprints, function (fingerprint) {
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);
    fetchBenchmarks(benchmarkName, fingerprint, myToken);
});
```

* This will access all of the method "fingerprints" from your provided report which are then used to access the CyBench
  hosted benchmarks (fingerprints are the UUIDs that help to identify benchmarks)
    * myFingerprintsAndNames is a Java HashMap that maps 'method fingerprint' to 'method name'
    * myFingerprints is a Java ArrayList of the method fingerprints

 ```js
 // COMPARATOR CONFIGURABLES //
var currentVersion = getCurrentVersion();
var previousVersion = getPreviousVersion();
var threshold = Comparisons.Threshold.PERCENT_CHANGE;
var range = 1;
var percentChangeAllowed = 10;

var currentVersionScores;
var previousVersionScores;
 ```

* This is where you will be able to customize the type of comparison you will run.
    * More information in [Customization](#customization)

```js
forEach.call(myFingerprints, function (fingerprint) {
    // get all benchmarks recorded for specified version (possible returns null!)
    currentVersionScores = getBenchmarksByVersion(fingerprint, currentVersion);
    previousVersionScores = getBenchmarksByVersion(fingerprint, previousVersion);
    var benchmarkName = myFingerprintsAndNames.get(fingerprint);

    if (currentVersionScores != null && previousVersionScores != null) {
        // loop through each benchmarked mode within this version
        currentVersionScoreModes = new ArrayList(currentVersionScores.keySet());
        compareVersionScoreModes = new ArrayList(previousVersionScores.keySet());
        forEach.call(currentVersionScoreModes, function (mode) {
            if (compareVersionScoreModes.contains(mode)) {
                var percentChange = deltaCompareBetweenVersions(currentVersionScores.get(mode), previousVersionScores.get(mode), threshold, range);
                var pass = passAssertionPercentage(percentChange, percentChangeAllowed);
                print(benchmarkName + " : " + mode + " - Between version " + currentVersion + " and " + previousVersion + ", the percent change in last value recorded was " + percentChange + "%");
                if (pass) {
                    print("Passed test\n");
                } else {
                    print("FAILED test\n");
                }
            }
        });
    }
});
```

* This is the bulk of the comparison/assertion logic
    * First, we loop through the versions of each fingerprint
        * `getBenchmarksByVersion(fingerprint, currentVersion)` will return a Java HashMap that maps the 'benchmark
          version' to another Java HashMap of 'benchmark modes' that have been run
        * Note: we have to check to make sure there are benchmarks for both the current version and previous version of
          the currently looped fingerprint (Null Pointer Exception exception). It is possible that you have benchmarks
          that only have tests in one of the two versions being compared (for this between version test).
    * So, we loop through the modes of each version
        * Note: we have to check to make sure both versions have a test within the mode being looped through for the
          same reason as before
    * Next come the comparisons and assertions
        * `var percentChange = deltaCompareBetweenVersions(currentVersionScores.get(mode), previousVersionScores.get(mode), threshold, range);`
          calls a `delta` compare method that has been defined by [exposed methods](#exposed-methods-for-use) mentioned
          below
            * It compares the current verison scores under a specific mode to the previous version scores under the same
              mode.
            * The comparison is marked with a threshold of `PERCENT_CHANGE` which means we are looking for a percent
              change in value as opposed to a straight delta value
            * The comparison is marked with a range of `1` which means we are only looking at the last (most recent)
              value of `previousVersionScores.get(mode)` and comparing it to the most recent score
              in `currentVersionScores.get(mode)`
        * `var pass = passAssertionPercentage(percentChange, percentChangeAllowed);` calls an assertion method that
          checks to see if the `percent change` is within the `percentChangeAllowed` specified by you
        * Finally, there are some print statements to help visualize the actual calculations and assertions

### Customization

* `myReport:` The file path to your .cybench report that you want to be analyzed
* `myToken:` The CyBench query token that is required to access previous benchmarks hosted by CyBench
* Under `/// COMPARATOR CONFIGURABLES ///`
    * You will be able to define different variables that you can pass to
      the [exposed methods](#exposed-methods-for-use)
* `SCOPE`
    * Comparator gives you the ability to compare WITHIN and BETWEEN (current and previous) versions, just grab the versions with the
	`getCurrentVersion()` and `getPreviousVersion()` methods and pass the values accordingly
* `RANGE`
    * You can specify the amount of values you want to compare to. This can be any integer, or the String `ALL` to
      compare against all possible values in the version. There is handling behind the scenes if your range is too high
        * If it is too high, Comparator will compare against as many values as possible and treat range as `ALL`
    * If range is `1`, then only the last value will be compared to
        * If range is higher than 1, then the mean of the last X specified values will be taken and used for comparison
* `METHOD`
    * The comparison methods you have access to are `delta methods` and `standard deviations methods`
        * For standard deviation methods, you can specify a `deviationsAllowed` for assertions that will check to make
          sure the new score is within that amount of deviations from the mean of the scores being compared to
        * For delta methods, you can specify a `threshold`
            * Threshold is defined with a Java Class Enum and the options you have are `Comparisons.Threshold.GREATER`
              and `Comparisons.Threshold.PERCENT_CHANGE`
            * `Comparisons.Threshold.GREATER` will be used for tests in which you want raw score comparisons and strict
              deltas
            * `Comparisons.Threshold.PERCENT_CHANGE` will be used for tests in which you want to measure the percent
              change of the score in comparison to the compare to scores
                * If you choose to use these methods, you can use a `percentageAllowed` variable to run assertions and
                  make sure your new score is within X percent of the compared to scores

### Exposed Methods for Use

* [Exposed Methods](src/main/resources/ComparatorScriptBindings.js)
    * These methods can be called in your .js script

* `getFingerprintsFromReport` will give you a `Java Map` of the fingerprints within your provided report (used for
  fetching more benchmarks) - maps to benchmark names
* After you call `fetchBenchmarks` which fetches all benchmarks from the CyBench database
    * `getAllBenchmarks`, `getBenchmarksByFingerprint`, `getBenchmarksByVersion`, `getBenchmarksByMode`, are different
      ways to access the benchmarks stored in `Java Maps`
* `getCurrentVersion` and `getPreviousVersion` fetch the most recent version found in your benchmarks the previous version you used to that
* `deltaCompareWithinVersion`, `sdCompareWithinVersion`, `deltaCompareBetweenVersions`, and `sdCompareBetweenVersions`
  are compare methods you can call with your scores that run all calculations behind the scenes and return `Double`
  values
* `calculateDelta`, `calculateMean`, `calculateSD`, and `calculatePercentChange` are simple methods you can quickly
  access for your own calculations and return `Double` values
* `passAssertionDeviation`, `passAssertionPercentage`, and `passAssertionPositive` are assertion methods you can use to
  return `boolean` values that represent pass/fail

## YAML Configuration

### Cybench Comparator configuration

#### Configuration Variables

* Configuration file [comparator.yaml](config/comparator.yaml)
* The first two configurations are vital to fetching the previous benchmark scores correctly
    * `reports:` The location of the CyBench reports folder for your repository
        * If running the Comparator from the root of your project, `reports: "reports/"` shall suffice as reports are by
          default generated into `~/reports`
    * `token:` Set this to your CyBench Access token, specifically a 'query' one. You can generate an access token for
      your private workspace on the CyBench website. More details and a guide is
      provided [here](https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-private-workspaces).
* The following branches of `comparator.yaml` are used for configuring values exclusive to a certain package. i.e. If
  you'd like to test for change in Delta between the last benchmark for one package, and then test for change in average
  compared to ALL previous benchmarks in another package, you can! Multiple branches are defined by `compare.X`
  where `X` is any arbitrary name that you choose. While `compare.default` values should be changed to your liking, the
  name `compare.default` itself should **NOT** be adjusted.
* An example has been given below of setting up different packages

```yaml
compare.default:
  method: "DELTA"
  scope: "BETWEEN"
  threshold: "GREATER"
  percentage: "1"
  range: "LAST_VALUE"
  version: "1.0.1"
compare.A:
  method: "MEAN"
  package: "calctest.ClockTest"
  scope: "WITHIN"
  threshold: "percent_change"
  percentage: "15"
  range: "ALL_VALUES"
```

In the above example, the package `calctest.ClockTest` and all its benchmarks will test for a percent change of no less
than -15% or better, it'll also compare all previous benchmarks for this package version and its tests. Other tested
packages will refer to the `compare.default` since they are not explicitly defined by a `package:` value. This means all
other packages in your Comparator run will test for the change in score between your most recent score, and the previous
score. In this case, `threshold:` is set to `"GREATER"`, which means the most recent score must be greater than the
previous in order to pass. As opposed to `compare.A`, `compare.default` will check scores from a different version (in
this example, it'll compare scores between the current version, and `version: "1.0.1"`.

* Inside these `compare.X` branches exists various configurations you can set.

##### Comparator Methods

* The first configuration you should decide is the method to compare
* Comparison method is defined by `method:`
* The possible values for `method:` are listed below
    * `DELTA` = Tests if newest benchmark scores higher than the previous
    * `MEAN` = Takes the average of X previous scores (where X is defined by `range:`) and compares average to the
      newest score
    * `SD` = Tests if the newest score is within the standard deviation of X previous scores (where X is defined
      by `range:`)

#### Package to Compare

* The next configuration to decide is which package should be tested
    * Setting this value is crucial to taking advantage of multiple `compare.X` branches
* Only used when defining multiple `compare.X` branches, does not get defined in `compare.default`
* Package is defined with `package:`
* Must be set to the full package name, e.g. `package:"com.calcTest"`

#### Comparison Scope

* This configuration is used for testing either within or between versions
* Scope is defined by `scope:`
* Possible values are `"WITHIN"` or `"BETWEEN"`
    * `"WITHIN"` = Compare scores within the same version of your project
    * `"BETWEEN"` = Compare scores between different versions of your project
* **NOTE:** When using the `"BETWEEN"` scope, make sure to also set `version:` to whichever version you wish to compare
  to

#### Comparison Threshold

* This configuration will decide what values dictate if your build/test passes or fails
* Threshold is defined by `threshold:`
* Possible values are either `"GREATER"` or `"PERCENT_CHANGE"`
    * `"GREATER"` = Passes/fails depending on if your current score was higher than the score getting compared against (
      whether it's MEAN, DELTA, etc.)
    * `"PERCENT_CHANGE"` = More flexible, allows the build/test to pass even if the score was lower, as long as it is
      within a given percentage
* **NOTE:** When using `"PERCENT_CHANGE"`, make sure to define `percentage:"X"`, where X is the percent change allowed,
  even if the comparison results in a negative number

#### Comparison Range

* Setting this configuration will allow you to choose what your newest score compares against
* Possible values for range are `"ALL"` and any integer X`
    * `"ALL"` = Compare newest score to only the previous one
    * `X` = Compare newest score to the previous X scores

#### Example `comparator.yaml`

A template `comparator.yaml` can be taken from this repository, and can/should be used for your own tests. If you've
added the CyBench comparator to your project via this README or the CyBench Wiki, Comparator will look
for `comparator.yaml` in a folder called `config/` at the root of your project. All CyBench components that use a
properties or configuration file will look for those files inside this same folder. The template `comparator.yaml` also
includes comments at the top to help you adjust values on the fly. Once you've set your configurations, you're ready for
the next step of running the Comparator, detailed in the next section. Below is an example of a more fleshed
out `comparator.yaml`

```yaml
### Property File for Cybench Comparator Tool ###

# reports = location of CyBench reports folder

# token = CyBench Access Token with View Permissions
### Token can be found on the CyBench site by clicking on your username ###
### Token should be a query token with access to the specific workspace that stores your benchmarks ###

# compare.default = default comparison configurations

#compare.{} = specific comparison configurations 
### {} can be any identifier of your choice ###
### Make sure to include {package} to specify which package you want these comparisons to run on ###


### Comparison Configurations ###

# scope = (within or between project versions)
## Options {within, between} ##
### {within} will compare all benchmarks within the benchmarked version ###
### if {between} is chosen, must specify {version} (will compare benchmarked version to the specified version) ###
### add {version} to specify which version to compare to ###

# range = {amount of values to compare against}
## Options {all, (#)} - can specify the word "all" to compare against all values or any number X to compare against previous X recorded scores ##
### to compare against just the previous score within your version or the most recent score in another version, specify range '1' ###
### otherwise the new score will be compared against the mean of the previous X values ###

# method = how benchmarks will be compared
## Options {delta, SD} ##
### if {SD} is chosen, must specify {deviationsAllowed} ###
### {deviationsAllowed} = the amount of deviations you will allow your score to be away from the mean of the previous X values (X specified as {range}) ###
### if {delta} is chosen, must specify {threshold} ###
# {threshold} = how to specify a passing benchmark 
## Options {percent_change, greater} ##
### {greater} will check to see if new score is simply greater than the compared to score ###
### if {percent_change} is chosen, must specify {percentageAllowed} ###
### {percentageAllowed} = percentage score should be within in order to pass ###
### ex. 5% means the new score should be within 5% of the previous threshold ###


reports: "C:/Users/MUSR/eclipse-workspace/myMavenProject/reports/"
token: "ws_874a4eb4-fzsa-48fb-pr58-g8lwa7820e132_query"

compare.default:
  method: "DELTA"
  scope: "BETWEEN"
  version: "1.0.0"
  range: "ALL"
  threshold: "greater"

compare.A:
  package: "com.my.package"
  method: "SD"
  deviationsAllowed: "1"
  scope: "WITHIN"

compare.B:
  package: "com.my.other.package"
  threshold: "PERCENT_CHANGE"
  percentageAllowed: "10"
  range: "2"
```

#### Application

* Main class: `com.gocypher.cybench.CompareBenchmarks`

## Running Cybench Comparator

### From Maven

* Step 1: to run Cybench Comparator from Maven, edit POM of your project first by adding this profile:
    ```xml
    <project>
        <profiles>
            <profile>
                <id>compareBenchmarks</id>
                <!-- @@@ Maven central snapshots repository to get dependency artifacts snapshot releases @@@ -->
                <repositories>
                    <repository>
                        <id>oss.sonatype.org</id>
                        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
                        <releases>
                            <enabled>false</enabled>
                        </releases>
                        <snapshots>
                            <enabled>true</enabled>
                        </snapshots>
                    </repository>
                </repositories>
                <dependencies>
                    <!-- @@@ Cybench Comparator app dependency @@@ -->
                    <dependency>
                        <groupId>com.gocypher.cybench.client</groupId>
                        <artifactId>gocypher-cybench-comparator</artifactId>
                        <version>1.0.0-SNAPSHOT</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
                <properties>
                    <!-- ### Java executable to use ### -->
                    <comp.java.home>${java.home}</comp.java.home>
                    <comp.java.exec>"${comp.java.home}/bin/java"</comp.java.exec>
                    <comp.class>com.gocypher.cybench.CompareBenchmarks</comp.class>
                    <comp.class.args>cfg=config/comparator.yaml</comp.class.args>
                </properties>
                <build>
                    <plugins>
                        <!-- @@@ Make classpath entries as properties to ease access @@@ -->
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-dependency-plugin</artifactId>
                            <version>3.1.2</version>
                            <executions>
                                <execution>
                                    <id>get-classpath-filenames</id>
                                    <goals>
                                        <goal>properties</goal>
                                    </goals>
                                </execution>
                                <execution>
                                    <phase>generate-sources</phase>
                                    <goals>
                                        <goal>build-classpath</goal>
                                    </goals>
                                    <configuration>
                                        <outputProperty>comp.compile.classpath</outputProperty>
                                        <pathSeparator>${path.separator}</pathSeparator>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                        <plugin>
                            <groupId>org.codehaus.mojo</groupId>
                            <artifactId>exec-maven-plugin</artifactId>
                            <version>3.0.0</version>
                                <!-- @@@ Compare benchmarks @@@ -->
                                <execution>
                                    <id>compare-benchmarks</id>
                                    <goals>
                                        <goal>exec</goal>
                                    </goals>
                                    <!-- ### Maven phase when to compare benchmarks ### -->
                                    <phase>integration-test</phase>
                                    <configuration>
                                        <executable>${comp.java.exec}</executable>
                                        <classpathScope>test</classpathScope>
                                        <commandlineArgs>
                                            -cp ${comp.compile.classpath} ${comp.class} ${comp.class.args}
                                        </commandlineArgs>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
                </build>
            </profile>
            <...>
        </profiles>
        <...>
    </project>
    ```
  **Note:** configurable sections are marked with comments starting `<!-- ###`.

  **Note:** you may need configuration file
  [comparator.yaml](config/comparator.yaml). Put it somewhere in your project scope and set it over `comp.class.args`
  property:
    ```xml
    <comp.class.args>cfg=config/comparator.yaml</comp.class.args>
    ```

* Step 2: run your Maven script with `compareBenchmarks` profile enabled:
    ```cmd
    mvn clean verify -f pom.xml -P compareBenchmarks 
    ```

  **Note:**
    * `clean` - this goal is optional, but in most cases we want to have clean build
    * `verify` - this goal is used to cover full build process lifecycle, since our default benchmark build and run
      phases are bound to `pre-integration-test` and `integration-test`. But you may change accordingly to adopt your
      project build lifecycle, but **note** those phases must go after `test-compile` phase, since we are dealing with
      the product of this phase.
    * `-f pom.xml` - you can replace it with any path and file name to match your environment

### Gradle

* Step 1: to run Cybench Comparator from Gradle, edit `build.gradle` of your project first by adding these `repository`,
  `configurations`, `dependnecies` and `task` definitions:
    * Groovy
        ```groovy
        repositories {
            mavenCentral()
            maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots' }
        }
        // ...
        configurations {
            cybenchComparator
        }
        // ...
        dependencies {
            // ...
            cybenchComparator 'com.gocypher.cybench.client:gocypher-cybench-comparator:1.0.0-SNAPSHOT'
        }
        // ...
        task compareBenchmarks(type: JavaExec) {
            group = 'CyBench-Comparator'
            description = 'Compare Benchmarks'
            classpath = files(
                    project.sourceSets.main.runtimeClasspath,
                    project.sourceSets.test.runtimeClasspath,
                    configurations.cybenchComparator
            )
            main = 'com.gocypher.cybench.CompareBenchmarks'
            args = [
              'cfg=config/comparator.yaml'
            ]
        }
        ```

    * Kotlin
        ```kotlin
        import java.util.Properties
        // ...
        repositories {
          mavenCentral()
          maven {
            setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots")
          }
        }
        // ...
        val cybenchComparator by configurations.creating {
          isCanBeResolved = true
          isCanBeConsumed = false
        }
        // ...
        dependencies {
          // ...
          cybenchComparator ("com.gocypher.cybench.client:gocypher-cybench-comparator:1.0.0-SNAPSHOT")
        }
        // ...
  
        tasks {
          val compareBenchmarks by registering(JavaExec::class) {
            group = "cybench-comparator"
            description = "Compare Benchmarks"
            javaLauncher.set(launcher)
  
            classpath(
              sourceSets["main"].runtimeClasspath,
              sourceSets["test"].runtimeClasspath,
              configurations.getByName("cybenchComparator")
            )
            
            mainClass.set("com.gocypher.cybench.CompareBenchmarks")
            args ("cfg=config/comparator.yaml")
          }
        }
        ```

  **Note:** since `gocypher-cybench-comparator` now is in pre-release state, you have to add maven central snapshots
  repo `https://s01.oss.sonatype.org/content/repositories/snapshots` to your project repositories list.

  **Note:** you may need configuration file
  [comparator.yaml](config/comparator.yaml). Put it somewhere in your project scope and set it over `cfg`
  property:
    ```cmd
    "cfg=config/comparator.yaml"
    ```

* Step 2: run your Gradle script:
    * To compare benchmarks
      ```cmd
      gradle :compareBenchmarks 
      ```

**Note:** If you want to run Compare Benchmarks:

* Make sure to update your Maven or Gradle build files with the defined build
  in [gocypher-cybench-comparator](https://github.com/K2NIO/gocypher-cybench-java/blob/master/gocypher-cybench-client/gocypher-cybench-comparator/README.md)
* For Maven, place these execution phases before the compareBenchmarks execution phase
* For Gradle / Kotlin, add `dependsOn` to the compareBenchmarks task to make it run after the others
