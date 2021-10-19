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
  `reports` = the location of the CyBench reports folder for your repository  
  `method` = the method you would like to use for comparing  
  `workspace` = (optional) the name of your CyBench workspace   
  `token` = (optional) your GitHub personal access token for authentication

#### Comparator Methods

* All comparisons are done within the same version release  
  `Delta` = Tests if newest benchmark is quicker than previous benchmark  
  `Mean` = Tests if newest benchmark is quicker than the average scores of the previous benchmarks  
  `SD` = Tests if newest benchmark exceeds the accepted standard deviation of previous tests  
  `Moving_Average` = Tests if newest benchmark has lowered the moving average of the previous 5 benchmarks

#### Application

* Main class: `com.gocypher.cybench.CompareBenchmarks`

## Running Cybench Comparator

### From Maven

* Step 1: to run Cybench Comparator from Maven, edit POM of your project first by adding this profile:
    ```xml
    <project>
        <profiles>
            <profile>
                <id>test-2-bench</id>
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

* Step 2: run your Maven script with `test-2-bench` profile enabled:
    ```cmd
    mvn clean verify -f pom.xml -P test-2-bench 
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
        import java.util.Properties;
        // ...
        repositories {
          mavenCentral();
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
