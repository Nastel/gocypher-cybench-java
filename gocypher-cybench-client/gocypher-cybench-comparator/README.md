# gocypher-cybench-comparator

This app is designed to compare recent CyBench reports in your project to previously ran CyBench benchmarks hosted on
the CyBench site. The following README information will help you get started in understanding and using the CyBench
Comparator. For a more in-depth overview, review
the [Comparator Page](https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-Comparator) on
the [CyBench Wiki](https://github.com/K2NIO/gocypher-cybench-java/wiki)

Dependencies for your project:

* Maven:
    ```xml
    <dependency>
        <groupId>com.gocypher.cybench.client</groupId>
        <artifactId>gocypher-cybench-comparator</artifactId>
        <version>1.3.1</version>
        <scope>test</scope>
    </dependency>
    ```

* Gradle:
    ```groovy
    runtime 'com.gocypher.cybench.client:gocypher-cybench-comparator:1.3.1'
    ```

## Running Cybench Comparator

### Application

* Main class: `com.gocypher.cybench.CompareBenchmarks`

### From CMD

```cmd
java -jar gocypher-cybench-comparator.jar [args]
```

### From Maven

* Step 1: to run Cybench Comparator from Maven, edit POM of your project first by adding this profile:
    ```xml
    <project>
        <profiles>
            <profile>
                <id>compareBenchmarks</id>
                <dependencies>
                    <!-- @@@ Cybench Comparator app dependency @@@ -->
                    <dependency>
                        <groupId>com.gocypher.cybench.client</groupId>
                        <artifactId>gocypher-cybench-comparator</artifactId>
                        <version>1.3.1</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
                <properties>
                    <!-- ### Java executable to use ### -->
                    <comp.java.home>${java.home}</comp.java.home>
                    <comp.java.exec>"${comp.java.home}/bin/java"</comp.java.exec>
                    <comp.class>com.gocypher.cybench.CompareBenchmarks</comp.class>
                    <comp.class.args>-C config/comparator.yaml</comp.class.args>
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
                            <executions>
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
    <comp.class.args>-C config/comparator.yaml</comp.class.args>
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

### From Gradle

* Step 1: to run Cybench Comparator from Gradle, edit `build.gradle` of your project first by adding these `repository`,
  `configurations`, `dependnecies` and `task` definitions:
    * Groovy
        ```groovy
        // ...
        configurations {
            cybenchComparator
        }
        // ...
        dependencies {
            // ...
            cybenchComparator 'com.gocypher.cybench.client:gocypher-cybench-comparator:1.3.1'
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
              '-C config/comparator.yaml'
            ]
        }
        ```

    * Kotlin
        ```kotlin
        // ...
        val cybenchComparator by configurations.creating {
          isCanBeResolved = true
          isCanBeConsumed = false
        }
        // ...
        dependencies {
          // ...
          cybenchComparator ("com.gocypher.cybench.client:gocypher-cybench-comparator:1.3.1")
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
            args ("-C config/comparator.yaml")
          }
        }
        ```

  **Note:** you may need configuration file
  [comparator.yaml](config/comparator.yaml). Put it somewhere in your project scope and set it with flag `-C`
  or `-configPath` property:
    ```cmd
    "-C config/comparator.yaml"
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

## Adding Comparator to Jenkins

Comparator has the ability to fail a Jenkins build in the case of comparison failures. Just add a Jenkins stage with a
Comparator run command for your appropriate operating system.
For different run configurations, refer to [running the comparator](#running-cybench-comparator).

### Windows

```jenkinsfile
stage('Compare Benchmarks') {
            steps {
                bat 'gradle :compareBenchmarks'
            }
        }
```

### Linux

```jenkinsfile
stage('Compare Benchmarks') {
            steps {
                sh 'gradle :compareBenchmarks'
            }
        }
```

## Configuration

### Dedicated Java System properties

* `cybench.reports.view.url` - defines CyBench service endpoint URL to obtain benchmark reports. Default value - 
  `https://app.cybench.io/gocypher-benchmarks-reports/services/v1/reports/benchmark/view/`.
* `cybench.benchmark.base.url` - defines default CyBench benchmark report base URL. Default value - 
  `https://app.cybench.io/cybench/benchmark/`.

### Configuration Args

* **NOTE**
    * `-C` must be specified for using [YAML Configuration](#yaml-configuration), the rest of the arguments are used
      for [Scripting Configuration](#scripting). (args can be specified within YAML file)

| Argument Flag |  `.yaml` Equivalent | Valid Options | Description
| --- | --- | --- | --- |
| -F, -failBuild | `failBuild:` | N/A | This argument is unique in that you don't need to pass a value with it. Default value is `false`, meaning your build will **not** fail even if one more multiple benchmark comparison tests fail. By passing the (-f) flag, this value gets set to `true`, meaning your build **will** fail if even just one benchmark comparison test fails. | 
| -C, -configPath | N/A | An existing `comparator.yaml` config file | Allows you to forgo scripting and specify the path of a valid `comparator.yaml` configuration file | 
| -S, -scriptPath | N/A | An existing `.js` script | Specify file path/name of the script | 
| -T, -token | `token:` | An existing CyBench query access token | Specify your CyBench Workspace's query access token | 
| -R, -reportPath | `reportPath: ` | Path to folder containing CyBench generated reports, or a specific report | Specify a certain `.cybench` report, or a folder of them |
| -s, -scope | `scope:` | `WITHIN` or `BETWEEN` | Choose between comparing within current version, or between previous versions, when using `BETWEEN`, a specific version can be specified with (-v), otherwise defaults to the previous version |
| -v, -compareVersion | `compareVersion:` | `PREVIOUS` or any specific version | Specify the version you'd like to compare to, previous is the immediate version prior to the tested version, e.g. a Benchmark with the version `2.0.2` compared to the `PREVIOUS` version will compare to `2.0.1`|
| -r, -range | `range:` | `ALL` or any integer | Decide how many scores you'd like to compare the newest one to, `ALL` would be all values, `1` would be the previous score from the newest |
| -m, -method | `method:` | `DELTA` or `SD` | Decide which method of comparison to use. `DELTA` will compare difference in score, and requires an additional flag, threshold (-t). `SD` will do comparisons regarding standard deviation. `SD` requires an additional flag as well, deviations allowed (-d) |
| -d, -deviationsAllowed | `deviationsAllowed:` | Any Double value | Used with assertions to check that the new score is within the given amount of deviations from the mean. (mean being calculated from the scores being compared to) |
| -t, -threshold | `threshold:` | `GREATER` or `PERCENT_CHANGE` | Only used with the `DELTA` method. `GREATER` will compare raw scores, `PERCENT_CHANGE` is used to measure the percent change of the score in comparison to previous scores. |
| -p, -percentChangeAllowed | `percentChangeAllowed:` | Any Double value | This argument is used when running assertions, makes sure your new score is within X percent of the previous scores you're comparing to |

## Scripting

Template scripts located in the scripts folder [scripts](scripts/)

* The CyBench Comparator tool allows you to build your own customized .js files that hit benchmark fetch methods,
  comparison methods, and assertion methods.
    * This allows you more flexibility in what you do with your benchmark scores and comparison statistics.
* Certain variables are required for the Comparator to work. When you use a custom script, these variables are already
  set for you. Benchmarks are fetched in the background immediately once configuration arguments are passed to the main
  class. Refer to the [exposed methods](#exposed-methods-for-use) section to view accessible methods. Below is a list of
  variables accessible in the scripts:
    * `myBenchmarks` - a Java `Map` of all the benchmarks from your report. `myBenchmarks` is
      a `Map<String, Map<String, Map<String, Double>>>` object, which maps
      to `<Benchmark Fingerprint: <Version : <Mode : <Score>>>`
    * `myFingerprints` - an `ArrayList<String>` that contains every method's unique CyBench fingerprints in your report.
    * `fingerprintsToNames` - a `HashMap` that maps the aforementioned CyBench fingerprints to its corresponding
      method's name

The configuration arguments you pass via command line or build instructions (
see: [Configuration Args](#configuration-args)) are also accessible:

* `method` - the comparison method to be used
* `scope` - comparing between or within versions
* `range` - the amount of scores to compare to
* `threshold` - specify what constitutes a pass/fail in your test
* `percentChangeAllowed` - used with threshold `percent_change`, dictates how much percent change is allowed to
  pass/fail the test
* `deviationsAllowed` - used with `SD` `method` of comparison, amount of deviations allowed from the mean to pass/fail
  the test
* `compareVersion` - used when scope is `BETWEEN`, the version to compare to

### Script Configuring via args

* `FAIL BUILD` passed with `-F` or `-failBuild`
    * Passed as flag (no variable needed along with the flag)
    * Specifies whether you want the build to fail (CI/CD pipeline failure) if there are benchmark comparison failures
* `SCRIPT` passed with `-S` or `-scriptPath`
    * Specifies the file path to your script
* `TOKEN` passed with `-T` or `-token`
    * Specifies your CyBench query access token
* `REPORT` passed with `-R` or `-reportPath`
    * Specifies the report you want to analyze and run comparisons on, this can be the path to a single report, or the
      path to the full report directory (in which case the most recently ran report will be used)
* `SCOPE` passed with `-s` or `-scope`
    * Options: `WITHIN` or `BETWEEN`
    * Comparator gives you the ability to compare WITHIN or BETWEEN (current and previous) versions
        * For BETWEEN configurations, users can specify `compareVersion` as a version to compare to (against the current
          version)
            * `COMPARE VERSION` passed with `-v` or `-compareVersion`
* `RANGE` passed with `-r` or `-range`
    * Options: `ALL` or any Integer value
    * You can specify the amount of values you want to compare to. This can be any integer, or the String `ALL` to
      compare against all possible values in the version. There is handling behind the scenes if your range is too high
        * If it is too high, Comparator will compare against as many values as possible and treat range as `ALL`
    * If range is `1`, then only the last value will be compared to
        * If range is higher than 1, then the mean of the last X specified values will be taken and used for comparison
* `METHOD` passed with `-m` or `-method`
    * Options: `MEAN` or `SD`
    * The comparison methods you have access to are delta methods and standard deviations methods
        * For standard deviation methods, you can specify a `deviationsAllowed` for assertions. This will check to make
          sure the new score is within that amount of deviations from the mean of the scores being compared to
            * `DEVIATIONS ALLOWED` passed with `-d` or `-deviationsAllowed`
            * Options: Any Double value
        * For delta methods, you can specify a `threshold`
            * `THRESHOLD` passed with `-t` or `-threshold`
            * Options: `GREATER` or `PERCENT_CHANGE`
            * `GREATER` will be used for tests in which you want raw score comparisons and strict deltas
            * `PERCENT_CHANGE` will be used for tests in which you want to measure the percent change of the score in
              comparison to the compare to scores
                * If you choose to use these methods, you can use a `percentChangeAllowed` variable to run assertions
                  and make sure your new score is within X percent of the compared to scores
                    * `PERCENT CHANGE ALLOWED` passed with `-p` or `-percentChangeAllowed`
                    * Options: Any Double value

### Example Script

```javascript
// EXAMPLE ARGS PASSED VIA COMMAND LINE
// -F -S scripts/Delta-BetweenVersions-PercentChange.js -T ws_0a1evpqm-scv3-g43c-h3x2-f0pqm79f2d39_query -R reports/ -s BETWEEN -v PREVIOUS -r ALL -m DELTA -t PERCENT_CHANGE -p 10

// loop through the fingerprints in my report
forEach.call(myFingerprints, function (fingerprint) {
    var currentVersion = getCurrentVersion(fingerprint);
    var benchmarkName = fingerprintsToNames.get(fingerprint);
    var benchmarkedModes = getRecentlyBenchmarkedModes(fingerprint, currentVersion);

    // loop through the modes tested within the current version of the fingerprint (current version = version benchmarked with)
    forEach.call(benchmarkedModes, function (mode) {
        currentVersionScores = getBenchmarksByMode(fingerprint, currentVersion, mode);
        compareVersionScores = getBenchmarksByMode(fingerprint, compareVersion, mode);

        var percentChange = compareScores(benchmarkName, currentVersion, mode, currentVersionScores, compareVersionScores);
    });
});
```

Detailed below is a walkthrough of the script above, explaining what each line of code means, and what is happening in
the background as you execute the script.

### Template Walkthrough

* First, we loop through the fingerprints for each report
    * `getCurrentVersion(fingerprint)` is called first to establish the current version and set it to a
      variable `currentVersion`. The variable `fingerprint` is automatically set for you in the background.
    * The next line sets `benchmarkName` to the method name corresponding to the given fingerprint, it reads from
      the `fingerprintsToNames` HashMap.
    * After that, we call a function `getRecentlyBenchmarkedModes` that grabs an ArrayList `benchmarkedModes`, which
      contains the modes benchmarked by the specific fingerprint in the recent report.
    * **NOTE:** `myBenchmarks` is automatically defined for you, you do not have to set it manually.
* After these variables are set, another loop cycles through the modes tested within the current version
    * Two `List<Double>` objects are created, `currentVersionScores` and `compareVersionScores`. These lists of scores
      get populated via multiple calls of `getBenchmarksByMode(fingerprint, currentVersion/compareVersion, mode)`
    * **NOTE:** While `currentVersion` is set within the script, `compareVersion` has to be passed via arguments. The
      default compare version is the previous version of `currentVersion`. You can pass the compare version with the (
      -v) flag.
* Next come the comparisons and assertions
    * `var percentChange = compareScores(benchmarkName, currentVersion, mode, currentVersionScores, compareVersionScores);`
      calls a generalized compare method that has been defined by [exposed methods](#exposed-methods-for-use) mentioned
      below
        * It compares the current version scores under a specific mode to the previous version scores under the same
          mode. (`currentVersionScores`, `compareVersionScores`)
        * Method, threshold, and range are all gathered from the flags you passed via the command line (for this
          generalized compare method)
        * Method (`method`) is passed via the (-m) flag. In this example, method was set to `DELTA` which will allow the
          DELTA comparison to run.
        * Threshold ('threshold') is passed via the (-t)
          flag. In this example, threshold was set to `PERCENT_CHANGE` which will allow the test to pass even if it
          performs slower, as long as it is within a given `percentChangeAllowed` (-p)
        * Range (`range`) is passed via the (-r) flag. The range is `1` which means in this example, we are looking at
          the last (most recent) value of `currentVersionScores` and comparing it to the most recent score
          in `compareVersionScores`
* **NOTE:** As a reminder, a table of [passable arguments](#configuration-args)
  and [exposed methods](#exposed-methods-for-use) can be found below in their corresponding sections.

### Exposed Methods for Use

* [Exposed Methods](src/main/resources/ComparatorScriptBindings.js)
    * These methods can be called in your .js script

* `getAllBenchmarks`, `getBenchmarksByFingerprint`, `getBenchmarksByVersion`, `getBenchmarksByMode`, are different ways
  to access the benchmarks stored in `Java Maps`
* `getRecentlyBenchmarkedModes` will allow you to grab the modes benchmarked by the passed fingerprint and the version (
  currentVersion). It grabs from myBenchmarks which contains benchmark information from only your recent report (not
  including fetched information).
* `getCurrentVerison` and `getPreviousVersion` are methods that return version Strings of the fingerprint being
  compared. Current Version represents the current version of the fingerprint being benchmarked, and previous version
  will be passed back as the most previous version found in the fingerprint fetch based on dot notation. It is possible
  previous version returns null.
* `compareScores` is a generalized compare method which collects information from the command line flag arguments to
  decide which comparison method to run, for more specific comparisons, you can use the functions below
* `compareDelta`, `compareDeltaPercentChange`, and `compareSD` are specific compare methods you can call with your
  scores that run all calculations behind the scenes and return Double values
    * **NOTE:** When calling `compareSD`, you must supply `deviationsAllowed` (a `Double` type), example below:
        * `compareSD(benchmarkName, benchmarkVersion, benchmarkMode, range, deviationsAllowed, currentVersionScores, compareVersionScores)`
        * A filled out `compareSD()` may look like
          this: `compareSD(benchmarkName, benchmarkVersion, benchmarkMode, 5, 1, currentVersionScores, compareVersionScores)`
            * where `5` is the `range`, and `1` is the `deviationsAllowed`. Refer to `ComparatorScriptBindings.js` for
              full parameters/expectations.
    * **NOTE:** When calling `compareDeltaPercentChange`, you must supply `percentChangeAllowed`
        * `compareDeltaPercentChange(benchmarkName, benchmarkVersion, benchmarkMode, range, percentChangeAllowed, currentVersionScores, compareVersionScores)`
        * A filled out `compareDelta()` may look like
          this: `compareDelta(benchmarkName, benchmarkVersion, benchmarkMode, 5, 15, currentVersionScores, compareVersionScore)`
            * where `5` is the `range` and `15` is the `percentChangeAllowed`.
* `calculateDelta`, `calculateMean`, `calculateSD`, and `calculatePercentChange` are specific simple methods you can
  quickly access for your own calculations and return `Double` values
* `passAssertion` is a generalized assert method which collects information from the command line flag arguments to
  decide which assertion to run, for more specific assertions, you can use the functions below
* `passAssertionDeviation`, `passAssertionPercentage`, and `passAssertionPositive` are assertion methods you can use to
  return boolean values that represent pass/fail

## YAML Configuration

* Pass configuration file path via args
* Passed with `-C` or `-configPath`
* The rest of the flags defined previously for scripting are all defined within the actual YAML file

### Configuration Variables

```yaml
# failBuild = whether you want the build to fail (CI/CD pipeline failure) if there are benchmark comparison failures
## Options {true, false} ##

# reportPath = 
# location of CyBench reports folder (automatically takes the most recent report) 
# OR the location of the specific report you want to run Comparator on

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
### if {between} is chosen, must specify {compareVersion} (will compare benchmarked version to the specified version) ###
### add {compareVersion} to specify which version to compare to ###

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
### if {percent_change} is chosen, must specify {percentChangeAllowed} ###
### {percentChangeAllowed} = percentage score should be within in order to pass ###
### ex. 5% means the new score should be within 5% of the previous threshold ###
```

* Configuration file [comparator.yaml](config/comparator.yaml)
* The first two configurations are vital to fetching the previous benchmark scores correctly
    * `reportPath:` The location of the CyBench reports folder for your repository OR the location of the specific
      report you want to run Comparator on
        * If running the Comparator from the root of your project, `reportPath: "reports/"` shall suffice as reports are
          by default generated into `~/reports`
        * If report folder is passed, Comparator will use the most recent report in the folder
    * `token:` Set this to your CyBench Access token, specifically a 'query' one. You can generate an access token for
      your private workspace on the CyBench website. More details and a guide is
      provided [here](https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-private-workspaces).
* Additionally, you can pass a `failBuild` variable which will instruct Comparator to fail your build (i.e. fail your
  CI/CD pipeline) in the presence of failed comparisons
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
  range: "1"
  compareVersion: "1.0.1"
compare.A:
  package: "calctest.ClockTest"
  scope: "WITHIN"
  threshold: "PERCENT_CHANGE"
  percentChangeAllowed: "15"
  range: "ALL_VALUES"
```

In the above example, the package `calctest.ClockTest` and all its benchmarks will test for a percent change of no less
than -15% or better, it'll also compare all previous benchmarks for this package version and its tests. Other tested
packages will refer to the `compare.default` since they are not explicitly defined by a `package:` value. This means all
other packages in your Comparator run will test for the change in score between your most recent score, and the previous
score. In this case, `threshold:` is set to `"GREATER"`, which means the most recent score must be greater than the
previous in order to pass. As opposed to `compare.A`, `compare.default` will check scores from a different version (in
this example, it'll compare scores between the current version, and `compareVersion: "1.0.1"`).

* Inside these `compare.X` branches exists various configurations you can set.

#### Comparator Methods

* The first configuration you should decide is the method to compare
* Comparison method is defined by `method:`
* The possible values for `method:` are listed below
    * `DELTA` = Tests if newest benchmark scores higher than the previous X scores (if X is more than 1, will compare to
      mean of X)
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
* **NOTE:** When using the `"BETWEEN"` scope, make sure to also set `compareVersion:` to whichever version you wish to
  compare to

#### Comparison Threshold

* This configuration will decide what values dictate if your build/test passes or fails
* Threshold is defined by `threshold:`
* Possible values are either `"GREATER"` or `"PERCENT_CHANGE"`
    * `"GREATER"` = Passes/fails depending on if your current score was higher than the score getting compared against
    * `"PERCENT_CHANGE"` = More flexible, allows the build/test to pass even if the score was lower, as long as it is
      within a given percentage
* **NOTE:** When using `"PERCENT_CHANGE"`, make sure to define `percentChangeAllowed:"X"`, where X is the percent change
  allowed, even if the comparison results in a negative number

#### Comparison Range

* Setting this configuration will allow you to choose what your newest score compares against
* Possible values for range are `"ALL"` and any integer X`
    * `"ALL"` = Compare the latest score to only the previous one
    * `X` = Compare the latest score to the previous X scores

#### YAML Template

A template [comparator.yaml](config/comparator.yaml) can be taken from this repository, and can/should be used for your
own tests. If you've added the CyBench comparator to your project via this README or the CyBench Wiki, Comparator will
look for `comparator.yaml` in a folder called `config/` at the root of your project. All CyBench components that use a
properties or configuration file will look for those files inside this same folder. The template `comparator.yaml` also
includes comments at the top to help you adjust values on the fly. Once you've set your configurations, you're ready for
the next step of running the Comparator, detailed in the next section. Below is an example of a more fleshed
out `comparator.yaml`

```yaml
### Property File for Cybench Comparator Tool ###

failBuild: true
reportPath: "C:/Users/MUSR/eclipse-workspace/myMavenProject/reports/report-1633702919786-14.35352973095467.cybnech"
token: "ws_874a4eb4-fzsa-48fb-pr58-g8lwa7820e132_query"

compare.default:
  method: "DELTA"
  scope: "BETWEEN"
  compareVersion: "1.0.0"
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
  percentChangeAllowed: "10"
  range: "2"
```
