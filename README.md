# What is CyBench Launcher

**CyBench Launcher** is a standalone Java application designed to execute [JMH](https://openjdk.java.net/projects/code-tools/jmh/) benchmarks. 
Speed-test Java classes, collections, modules, libraries and other application building blocks under varying runtime conditions. 
Share your results with the community, compare and choose the right libraries for your application. 
**CyBench** helps developers build better, faster Java code by benchmarking code performance.

CyBench Launcher contains a set of default performance tests developed by the CyBench team to benchmark Java Core services. These tests are executed by default every time CyBench benchmarks are executed via `cybench` shell script and results are automatically submitted to a public [CyBench Repository](https://www.gocypher.com/cybench/).

Default benchmark execution and automated reporting can be configured via `<cybench-home>/conf/cybench-launcher.properties`
Download and run CyBench [here](https://github.com/K2NIO/cybench-java-benchmarks/releases).  Visit our [page](https://www.gocypher.com/cybench/) to analyze your results.

## Running User-defined Benchmarks using CyBench Launcher

#### Add the runner and JMH dependencies to your project

```xml
    <dependencies>
        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-generator-annprocess</artifactId>
            <version>1.29</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-core</artifactId>
            <version>1.29</version>
        </dependency>
        <dependency>
            <groupId>com.gocypher.cybench.client</groupId>
            <artifactId>gocypher-cybench-runner</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
```

#### Run your benchmarks
- Implement any benchmarks classes 
- Add call to the runner main class 
```java
    public static void main(String[] args) throws Exception {
        BenchmarkRunner.main(args);
    }
```
- Analyze results in your console, reports folder or [app.cybench.io](https://app.cybench.io/cybench/)
- **Optional:** Add CyBench configuration file into `<project-path>/conf/cybench-launcher.properties`  directory to manage your benchmarking run configuration. [Configuration example](https://github.com/K2NIO/gocypher-cybench-java/blob/master/gocypher-cybench-client/gocypher-cybench-runner/src/main/resources/cybench-launcher.properties)

#### CyBench launcher configuration

| Property name        | Description           | Default value  |
| ------------- |-------------| -----:|
| **javaOptions**      | All the property fields that starts with name javaOptions will be used while benchmarking as JVM properties. | - |
| **javaToUsePath**      | Provide full path to java.exe to be used e.g. D:/jdk180_162/bin/java.exe  | - |
| **benchmarks**| Provide jar's with JMH benchmarks which shall be executed with CyBench. [more here](adding-custom-benchmarks-for-execution)| - |
| **sendReport**| Choose if the report generated will be automatically uploaded. (true/false)  | true |
| **reportUploadStatus**| Define public or private property for the uploaded report visibility.  | public |
| **benchAccessToken** | By providing the "bench" token that you get after creating a workspace in CyBench UI, you can send reports to your private directory, which will be visible only to the users that you authorize. | - |
| **emailAddress** | Email property is used to identify report sender while sending reports to both private and public repositories | - |
| **reportName**| Choose the uploaded report name. E.g. | - |
| **benchmarkClasses**| Specify benchmarks by including fully qualified benchmark class names which are comma separated. For more information [more here](#execute-only-custom-benchmarks)| - |
| **numberOfBenchmarkForks**| Number of separate full executions of a benchmark (warm up+measurement), this is returned still as one primary score item. | 1 |
| **measurementIterations** | Number of measurements per benchmark operation, this is returned still as one primary score item. | 5 |
| **warmUpIterations**| Number of iterations executed for warm up.  |  1 |
| **warmUpSeconds**|  Number of seconds dedicated for each warm up iteration.  |  5  |
| **runThreadCount**| Number of threads for benchmark test execution. |  1 |
| **benchmarkMetadata**| A property which adds extra properties to the benchmarks report such as category or version or context. Configuration pattern is `<fully qualified benchmark class name>=<key1>:<value1>;<key2>:<value2>`. Example which adds category for class CollectionsBenchmarks: `com.gocypher.benchmarks.client.CollectionsBenchmarks=category:Collections;`   |   -  |
| **userProperties**| User defined properties which will be added to benchmarks report section `environmentSettings->userDefinedProperties` as key/value strings. Configuration pattern:`<key1>:<value1>;<key2>:<value2>`. Example which adds a project name:`user.propname1=My Test Project;` |  -  |

### Adding Custom Benchmarks for Execution

Update CyBench Launcher configuration located in `<cybench-home>/conf/cybench-launcher.properties`:

* __required__: add or update property `benchmarks`, set path to jar file which contains your JMH benchmark, this path will be added to the `CLASSPATH` of the JVM. Values must be semicolon separated!

    Rule:
    ```properties
    benchmarks=<path to custom jar file1>;<path to custom jar file2>;
    ```

    Example:
    ```properties
    benchmarks=gocypher-cybench-custom-1.0.0.jar;
    ```

* __optional__: register categories for your tests in order to have correct tests classification and better readability and comparison in CyBench portal. If not set then default value (`Custom`) will be written and all custom tests will reside under this category. Values of different classes must be semicolon separated!

    Rule:
    ```properties
    benchmarkMetadata=<fully classified benchmark class name>=category:<category name>;\
      <fully classified benchmark class name>=category:<category name>;
    ```

    Example:
    ```properties
    benchmarkMetadata=com.gocypher.benchmarks.client.CollectionsBenchmarks=category:Collections;
    ```

### Execute Only Custom Benchmarks

Update CyBench configuration in order to run only user-defined tests:
* add or update property `benchmarkClasses`, specify class names of tests which shall be executed (values must be comma separated).

    Rule:
    ```properties
    benchmarkClasses=<fully qualified class name, or class name>,<fully qualified class name, or class name>
    ```
    Example:
    ```properties
    benchmarkClasses=com.gocypher.benchmarks.client.CollectionsBenchmarks,NumberBenchmarks
    ```
