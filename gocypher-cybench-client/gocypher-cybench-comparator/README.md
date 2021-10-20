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

## Configuration

### Cybench Comparator configuration

#### Configuration Variables

* Configuration file [comparator.yaml](config/comparator.yaml)  
 * The first two configurations are vital to fetching the previous benchmark scores correctly
    * `reports:` The location of the CyBench reports folder for your repository  
      * If running the Comparator from the root of your project, `reports: "reports/"` shall suffice as reports are by default generated into `~/reports`
    * `token:` Set this to your CyBench Access token, specifically a 'query' one. You can generate an access token for your private workspace on the CyBench website. More details and a guide is provided [here](https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-private-workspaces).
 * The following branches of `comparator.yaml` are used for configuring values exclusive to a certain package. i.e. If you'd like to test for change in Delta between the last benchmark for one package, and then test for change in average compared to ALL previous benchmarks in another package, you can! Multiple branches are defined by `compare.X` where `X` is any arbitrary name that you choose. While `compare.default` values should be changed to your liking, the name `compare.default` itself should **NOT** be adjusted.
 * An example has been given below of setting up different packages

```yaml
compare.default:
    method: "DELTA"
    package: "calctest.CalculatorTest"
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
In the above example, the package `calctest.ClockTest` and all its benchmarks will test for a percent change of no less than -15% or better, it'll also compare all previous benchmarks for this package version and its tests. Other tested packages will refer to the `compare.default` since they are not explicitly defined by a `package:` value. This means all other packages in your Comparator run will test for the change in score between your most recent score, and the previous score. In this case, `threshold:` is set to `"GREATER"`, which means the most recent score must be greater than the previous in order to pass. As opposed to `compare.A`, `compare.default` will check scores from a different version (in this example, it'll compare scores between the current version, and `version: 1.0.1`.
 * Inside these `compare.X` branches exists various configurations you can set.
##### Comparator Methods
* The first configuration you should decide is the method to compare
* Comparison method is defined by `method:`
* The possible values for `method:` are listed below  
  * `DELTA` = Tests if newest benchmark scores higher than the previous 
  * `MEAN` = Takes the average of X previous scores (where X is defined by `range:`) and compares average to the newest score 
  * `SD` = Tests if the newest score is within the standard deviation of X previous scores (where X is defined by `range:`)
#### Package to Compare
* The next configuration to decide is which package should be tested
    * Setting this value is crucial to taking advantage of multiple `compare.X` branches
* Package is defined with `package:`
* Must be set to the full package name, e.g. `package:com.calcTest`
#### Comparison Scope
* This configuration is used for testing either within or between versions
* Scope is defined by `scope:`
* Possible values are `"WITHIN"` or `"BETWEEN"`
    * `"WITHIN"` = Compare scores within the same version of your project
    * `"BETWEEN"` = Compare scores between different versions of your project
* **NOTE:** When using the `"BETWEEN"` scope, make sure to also set `version:` to whichever version you wish to compare to
#### Comparison Threshold
* This configuration will decide what values dictate if your build/test passes or fails
* Threshold is defined by `threshold:`
* Possible values are either `"GREATER"` or `"PERCENT_CHANGE"`
    * `"GREATER"` = Passes/fails depending on if your current score was higher than the score getting compared against (whether it's MEAN, DELTA, etc.)
    * `"PERCENT_CHANGE"` = More flexible, allows the build/test to pass even if the score was lower, as long as it is within a given percentage
* **NOTE:** When using `"PERCENT_CHANGE"`, make sure to define `percentage:"X"`, where X is the percent change allowed, even if the comparison results in a negative number
#### Comparison Range
* Setting this configuration will allow you to choose what your newest score compares again
* Possible values for range are `"LAST_VALUE"`, `"LAST_5"`, and `"ALL_VALUES"`
    * `"LAST_VALUE"` = Compare newest score to only the previous one
    * `"LAST_5"` = Compare newest score to the previous 5 scores
    * `"ALL_VALUES"` = Compare newest score to **ALL** previous scores
#### Example `comparator.yaml`
A template `comparator.yaml` can be taken from this repository, and can/should be used for your own tests. If you've added the CyBench comparator to your project via this README or the CyBench Wiki, Comparator will look for `comparator.yaml` in a folder called `config/` at the root of your project. All CyBench components that use a properties or configuration file will look for those files inside this same folder. The template `comparator.yaml` also includes comments at the top to help you adjust values on the fly. Once you've set your configurations, you're ready for the next step of running the Comparator, detailed in the next section. Below is an example of a more fleshed out `comparator.yaml`

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

# method = how benchmarks will be compared
### Options {delta, mean, SD, moving_average} ###

# scope = (within or between project versions)
## Options {within, between} ##
### {within} will compare all benchmarks within the benchmarked version ###
### if {between} is chosen, must specify {version} (will compare benchmarked version to the specified version) ###
    ### add {version} to specify which version to compare to ###

# range = the amount of benchmarks within the scope to compare to
## Options {last_value, last_5, all_values} ##
### {last_value} will compare to last value, {last_5} will compare to last 5 values, {all_values} will compare to all values within scope ###

# threshold = how to specify a passing benchmark 
## Options {percent_change, greater} ##
### {greater} will check to see if new score is simply greater than the compared to score ###
### if {percent_change} is chosen, must specify {percentage} ###
    ### percentage = percentage score should be within in order to pass ###
    ### ex. 5% means the new score should be within 5% of the previous threshold ###


reports: "C:/Users/drigos/eclipse-workspace/calctest/reports/"
token: "ws_305a4eb4-cbda-44fb-ba58-g8eea7820e115_query"

compare.default:
    method: "MEAN"
    package: "calctest.CalculatorTest"
    scope: "BETWEEN"
    threshold: "GREATER"
    percentage: "1"
    range: "ALL_VALUES"
    version: "1.0.1"
compare.A:
    method: "MEAN"
    package: "calctest.ClockTest"
    scope: "WITHIN"
    threshold: "PERCENT_CHANGE"
    percentage: "15"
    range: "LAST_VALUE"
    
#compare.B:
  #  pacakge: "com.my.other.package"
  #  percentage: "6"
  #  threshold: "percent_change"
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
