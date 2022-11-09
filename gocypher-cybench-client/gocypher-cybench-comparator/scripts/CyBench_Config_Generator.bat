rem 
rem Copyright (C) 2020-2022, Nastel Technologies.
rem
rem This library is free software; you can redistribute it and/or
rem modify it under the terms of the GNU Lesser General Public
rem License as published by the Free Software Foundation; either
rem version 2.1 of the License, or (at your option) any later version.
rem
rem This library is distributed in the hope that it will be useful,
rem but WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
rem Lesser General Public License for more details.
rem
rem You should have received a copy of the GNU Lesser General Public
rem License along with this library; if not, write to the Free Software
rem Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
rem
rem

@echo off
setlocal enabledelayedexpansion

set curPath=%~dp0
set projectDir=%curPath%

:do
	cls
	if not exist "%projectDir%config\" mkdir "%projectDir%config"
	set "configDir=%projectDir%config\"
	echo CyBench Launcher Configuration Generator v0.1
	echo:
	echo Use this script file to generate configuration properties for all CyBench tools.
	echo Verify directories, and edit if needed. Output of .properties file(s) should be 
	echo placed in a folder called config, at the root of your project directory.
	echo:
	echo For best results, run this .bat file at the root of your project.
	echo:
	echo Options 2, 3, and 4 are used ^for generating cybench-launcher.properties
	echo Options 5, 6, and 7 are used ^for generating cybench-automation.properties
	echo:
    echo -----------------------------
    echo SETTINGS:
    echo        Script Home Dir: %curPath%
    echo  Project Root Dir Path: %projectDir%
    echo     Configurations Dir: %configDir%
    echo -----------------------------
	echo Choose option:
	echo 1... Modify Project Directory
	echo 2... Generate Basic Custom CyBench Launcher properties file    (Recommended)
	echo 3... Generate Full Custom CyBench Launcher properties file     (Advanced)
	echo 4... Generate Blank CyBench Launcher properties file           (Template)
	echo 5... Generate Basic Automated Comparison properties file       (Recommended)
	echo 6... Generate Custom Automated Comparison properties file            
	echo 7... Generate Blank Automated Comparison properties file       (Template)
	echo 8... Display help/info for this tool
	echo 9... Exit
	
	set /p sel=Select an option: 
		if [%sel%] == [1] (
			rem Modify Project Directory, config directory will update automatically
			echo:
			echo Setting new project directory. Config directory will update automatically.
			echo Please set new directory with ending "\".
			echo Current project directory is: %projectDir%
			echo:
			set /p projectDir="Enter new project directory: "
			echo: 
			goto doneModDir
			)
		if [%sel%] == [2] (
			echo:
			echo -----------------------------
			echo Generating basic custom CyBench Launcher Properties file ^(cybench-launcher.properties^)
			echo You will only be prompted for required fields, and default JMH settings will be used.
			echo JMH Settings ^(as well as all other properties^) can be changed manually after the file is generated.
			echo Please enter valid inputs for each prompts.
			echo -----------------------------
			echo:
			echo Set Access and Query tokens.
			echo This property is required for viewing/comparing results on CyBench's UI.
			echo Tokens are using for accessing and viewing Workspaces on CyBench's UI, which contain
			echo your reports and comparisons.
			echo Example ^(access token^): ws_86d80e94-ef7b-f3e3-63f5-a0147bc16c92_bench
			echo: 
			echo For more information on obtaining these tokens, view the video tutorial here: https://vimeo.com/681476714,
			echo ^or visit the CyBench Wiki here: https://github.com/Nastel/gocypher-cybench-java/wiki/Getting-Started-with-Private-Workspaces
			echo:
			set /p benchAccessToken="Enter benchAccessToken (ends with _bench): "
			set /p benchQueryToken="Enter benchQueryToken (ends with _query): "
			echo:
			echo Set user e-mail.
			echo This property is no longer required, but helpful to be associated with reports.
			echo Please note this is seperate from the e-mail set for notifications, which can be set on CyBench's UI.
			echo Example: drigos@nastel.com
			echo:
			set /p emailAddress="Enter e-mail address: "
			echo:
			echo Should report be uploaded to CyBench UI
			echo You have the option of sending benchmark reports upon completion, ^or just having reports
			echo saved locally. Set this value to true to have the report sent. For proper report upload,
			echo ensure tokens ^(bench, query^) were configured correctly.
			echo Example: true
			echo:
			set /p sendReport="Send Report? [(t)rue/(f)alse]: "
			echo:
			echo Set either private ^or public report upload.
			echo If sending report to CyBench, choose between public ^or private uploads.
			echo Private: Upload to workspace associated with bench tokens. Able to view, organize,
			echo and compare different builds/reports. Highly suggested.
			echo Public: Upload to CyBench UI's public repo. Can view, but cannot be organized,
			echo and cannot make use of CyBench UI's comparator feature. Public uploads are
			echo not recommended.
			echo Example: private
			echo:
			set /p reportUploadStatus="reportUploadStatus: [(pri)vate/(pub)lic]: "
			echo:
			echo Set report name
			echo Report name is essential for organizing reports, as well as for making comparisons
			echo on CyBench's UI. Helpful information such as build version/date in the report name are
			echo especially important when making manual comparisons on CyBench. 
			echo Example: ExplorerPackage_v1.0.5_build3201_103022
			echo:
			set /p reportName="reportName: "
			echo:
			
			set numberOfBenchmarkForks=1
			set measurementIterations=10
			set measurementSeconds=10
			set warmupIterations=5
			set warmupSeconds=10
			set runThreadCount=1
			set benchmarkModes=Throughput,SingleShotTime
			
			echo:
			echo Finished setting property values.
			
			goto checkOverwrite
			)
		if [%sel%] == [3] (
			echo:
			echo -----------------------------
			echo Generating fully custom CyBench Launcher Properties file ^(cybench-launcher.properties^)
			echo Please enter valid inputs for each prompts.
			echo Not all fields are required and will be designated.
			echo For properties you wish to skip, press Enter to continue to the next prompt
			echo -----------------------------
			echo:
			echo Set additional Java options, seperated by spaces ^(java Options^)
			echo This property is not required, and can be left blank.
			echo Example: -Xmx4096m -server --XX:NewSize=512m
			echo:
			set /p javaOptions="Enter javaOptions: "
			echo:
			echo Set Java version to be used ^(javaToUsePath^)
			echo This property is not required.
			echo By default, Java version will be used as set in environment variables.
			echo This property can be left blank if Java is already configured as an environment variable.
			echo If needing to use a different version of Java at runtime, set this property
			echo to the full path  of Java's java.exe
			echo Example: C:\Java\jdk-1.8.0_311\bin\java.exe
			echo:
			set /p javaToUsePath="Enter javaToUsePath: " 
			echo:
			echo Set benchmarks via .jar file
			echo This property is not required.
			echo If using this .properties file with CyBench IDE/build plug-ins ^(such as Eclipse, Maven, etc.^),
			echo this field can be left blank.
			echo This property is for running additional benchmarks, and will be nescessary
			echo if executing CyBench Launcher standalone, from commandline. Selected .jar file
			echo must have JMH benchmarks within, which will be executed with CyBench
			echo Example: ./benchmarks/myTestWithBenchmarks.jar
			echo:
			set /p benchmarks="Enter benchmarks: "
			echo:
			echo Set Access and Query tokens.
			echo This property is required for viewing/comparing results on CyBench's UI.
			echo Tokens are using for accessing and viewing Workspaces on CyBench's UI, which contain
			echo your reports and comparisons.
			echo Example ^(access token^): ws_86d80e94-ef7b-f3e3-63f5-a0147bc16c92_bench
			echo: 
			echo For more information on obtaining these tokens, view the video tutorial here: https://vimeo.com/681476714,
			echo ^or visit the CyBench Wiki here: https://github.com/Nastel/gocypher-cybench-java/wiki/Getting-Started-with-Private-Workspaces
			echo:
			set /p benchAccessToken="Enter benchAccessToken (ends with _bench): "
			set /p benchQueryToken="Enter benchQueryToken (ends with _query): "
			echo:
			echo Set user e-mail.
			echo This property is no longer required, but helpful to be associated with reports.
			echo Please note this is seperate from the e-mail set for notifications, which can be set on CyBench's UI.
			echo Example: drigos@nastel.com
			echo:
			set /p emailAddress="Enter e-mail address: "
			echo:
			echo Should report be uploaded to CyBench UI
			echo You have the option of sending benchmark reports upon completion, ^or just having reports
			echo saved locally. Set this value to true to have the report sent. For proper report upload,
			echo ensure tokens ^(bench, query^) were configured correctly.
			echo Example: true
			echo:
			set /p sendReport="Send Report? [(t)rue/(f)alse]: "
			echo:
			echo Set either private ^or public report upload.
			echo If sending report to CyBench, choose between public ^or private uploads.
			echo Private: Upload to workspace associated with bench tokens. Able to view, organize,
			echo and compare different builds/reports. Highly suggested.
			echo Public: Upload to CyBench UI's public repo. Can view, but cannot be organized,
			echo and cannot make use of CyBench UI's comparator feature. Public uploads are
			echo not recommended.
			echo Example: private
			echo:
			set /p reportUploadStatus="reportUploadStatus: [(pri)vate/(pub)lic]: "
			echo:
			echo Set report name
			echo Report name is essential for organizing reports, as well as for making comparisons
			echo on CyBench's UI. Helpful information such as build version/date in the report name are
			echo especially important when making manual comparisons on CyBench. 
			echo Example: ExplorerPackage_v1.0.5_build3201_103022
			echo:
			set /p reportName="reportName: "
			echo:
			echo Set specific benchmark classes
			echo This field is not required, but can be used to run only
			echo specific benchmark classes, if your project contains multiple classes
			echo of benchmarks. If opting to set this value, please enter the
			echo fully qualified benchmark class name^(s^), seperated via comma.
			echo Example: calc.logic.SmallCalculatorBenchmarks, calc.logic.BigCalculatorBenchmarks
			echo:
			set /p benchmarkClasses="benchmarkClasses: "
			echo:
			echo These next prompts will be for JMH specific settings. While these
			echo are not unique to CyBench, they very important for configuring
			echo correct and accurate benchmarks. All fields are required here.
			echo The following resource has a more
			echo in depth explanation of different fields, and possible values.
			echo https://blog.avenuecode.com/java-microbenchmarks-with-jmh-part-2
			echo:
			echo Set amount of benchmark forks
			echo This value decides how many seperate, full executions of a benchmark will occcure ^(warm up iteraions ^+ measurement iterations^)
			echo The score returned is still one primary value.
			echo Example: 1
			echo:
			set /p numberOfBenchmarkForks="numberOfBenchmarkForks: "
			echo:
			echo Set the amount of measurement iterations
			echo This dictates how many iterations of the specific benchmarked method will execute.
			echo Multiple iterations are suggested, and the score returned will be an overall score
			echo as a result of all iterations. For some tests/benchmarks ^(such as automated software tests^),
			echo only one iteration may be possible.
			echo Example: 10
			echo:
			set /p measurementIterations="measurementIterations: "
			echo:
			echo Set measurement seconds
			echo This value dictates how long each iteration will process for, before moving
			echo on to the next iteration. Different values may be needed here in order to 
			echo keep up with your code's execution. Setting this value to a low amount ^(1, ^or 2^) may be
			echo necessary for some automated software tests, while a higher value ^(9, ^or 10^) may be
			echo required for some code that requires a few seconds to complete.
			echo Example: 1
			echo:
			set /p measurementSeconds="measurementSeconds: "
			echo:
			echo Set warmup iterations
			echo Warmup iterations are important for getting the JVM "warmed up", meaning
			echo the first instances of your methods may need some time to achieve their max
			echo potential score. It is recommended to have a few warmup iterations for more 
			echo accurate and percise scores. Warmup iterations are the same as measurement iterations,
			echo but don't factor into the final score.
			echo Example: 5
			echo:
			set /p warmupIterations="warmupIterations: "
			echo:
			echo Set warmup seconds
			echo Similar to measurement seconds, this value dictates how long each warmup iteration will run for,
			echo used for warming up the JVM.
			echo Example: 10
			echo:
			set /p warmupSeconds="warmupSeconds: "
			echo:
			echo Set amount of threads
			echo This value dictates the amount of threads allocated to the benchmark process.
			echo Depending on your benchmarks/environment, it may be beneficial to use multiple threads.
			echo Example: 1
			echo:
			set /p runThreadCount="runThreadCount: "
			echo:
			echo Set Benchmark modes to run
			echo Multiple benchmark modes are possible via JMH. They include
			echo Throughput, Average time, Sample time, and Single shot time.
			echo Different modes are beneficial depending on what type of code/tests you
			echo are benchmarking. Multiple or all modes can be used, seperated via comma.
			echo To learn more about the different benchmark modes, and their use case, visit
			echo https://blog.avenuecode.com/java-microbenchmarks-with-jmh-part-2
			echo Example: Throughput
			echo Example 2: AverageTime,SingleShotTime
			echo Example 3: All
			echo:
			set /p benchmarkModes="benchmarkModes: "
			echo: 
			echo Set JMH command line arguments
			echo These additional JMH arguments are optional.
			echo All of the previous JMH values, along with extra options, can be set via command line
			echo flags. A full list of possible flags/options ^(found as a result of the -h option^), as well as their
			echo purpose and potential values, can be found at the following link. 
			echo https://github.com/guozheng/jmh-tutorial/blob/master/README.md#jmh-command-line-options
			echo Example: -f 1 -t 1 -wi 1 -w 5s -i 1 -r 5s -bm Throughput -bm SingleShotTime
			echo:
			set /p jmhArguments="jmhArguments: "
			echo:
			echo Finished setting property values.
			
			goto checkOverwrite
			)
		
		if [%sel%] == [4] (
			echo -----------------------------
			echo Generating blank ^(template^) cybench-launcher.properties.
			echo Please remember to fill in values before using with CyBench!
			echo -----------------------------
			
			goto checkOverwrite
			)
			
		if [%sel%] == [5] (
			echo:
			echo -----------------------------
			echo Generating Basic CyBench Automated Comparison Properties file ^(cybench-automation.properties^)
			echo:
			echo This option creates a basic, functional properties files ^for automated comparisons.
			echo It will compare against the most recent report, within the same version, and anomalies
			echo will be determined if the compared score ^(per benchmark^) has a percent change greater than 15%. Up to
			echo 2 anomalies will be allowed, before triggering an exception.
			echo This comparison properties file serves more as a demonstration/basic example, and can be modified after
			echo generation.
			echo -----------------------------
			
			set scope=within
			set compareVersion=
			set numLatestReports=1
			set anomaliesAllowed=2
			set method=DELTA
			set threshold=percent_change
			set percentChangeAllowed=15
			set deviationsAllowed=
			
			goto checkComparisonOverwrite
			
			)
		if [%sel%] == [6] (
			set compareVersion=
			set deviationsAllowed=
			set percentChangeAllowed=
			echo:
			echo -----------------------------
			echo Generating Basic CyBench Automated Comparison Properties file ^(cybench-automation.properties^)
			echo:
			echo This option creates a basic, functional properties files for automated comparisons.
			echo It will compare against the most recent report, within the same version, and anomalies
			echo will be determined if the compared score ^(per benchmark^) has a percent change greater than 15^%. Up to
			echo 2 anomalies will be allowed, before triggering an exception.
			echo This comparison properties file serves more as a demonstration/basic example, and can be modified after
			echo generation.
			echo -----------------------------
			echo:
			echo ^Set Comparison Scope
			echo Decide whether you wish to compare against the current version of your project, ^or a different version.
			echo Version is determined via build file ^(^for maven or gradle projects^), ^or manually ^set in the metadata
			echo of your benchmarks. Options ^for scope are within, ^for within the same version, ^or between, ^for comparing
			echo between two different versions.
			echo Valid options: within, between
			echo Example: within
			echo:
			set /p scope="scope: "
			echo:
			if !scope! == between (
				echo ^Set Compare Version
				echo When comparing between two versions of the same project, the compare version must be specified.
				echo ^For this configuration, please enter the project version ^(that has previous benchmark reports^)
				echo to compare against.
				echo Example: 1.0.5-beta
				echo:
				set /p compareVersion="compareVersion: "
				echo:
				)
			echo ^Set Number of Latest Reports
			echo This value dictates how many reports should be compared the most recent report/benchmark run.
			echo Setting this value to "1" will result in a comparison between your current benchmark run,
			echo and the most recent, previous benchmark run.
			echo Setting this value to a number greater than 1 will result in a comparison between your current benchmark run,
			echo and the average score of all reports specified. I.e., setting this value to "3" will result in a comparison
			echo between current run, and average score of the last 3 reports.
			echo NOTE: For standard deviation comparisons, you must have at least 3 reports.
			echo Valid options: any positive number
			echo Example: 1
			echo:
			set /p numLatestReports="numLatestReports: "
			echo:
			echo ^Set Anomalies Allowed
			echo This feature is especially useful ^if you're using benchmarks as a part of your CI/CD pipeline.
			echo After a benchmark run is completed, an automated comparison is made using the configurations set in cybench-automation.properties,
			echo ^if your comparison results in more anomalies than allowed ^(^set by this value^), an exception
			echo will be thrown, causing the pipeline to stop. ^(Users will be notified via e-mail, ^or communication platforms,
			echo ^if configured on CyBench UI^).
			echo Valid Options: 0, or any positive number
			echo Example: 5
			echo:
			set /p anomaliesAllowed="anomaliesAllowed: "
			echo:
			echo ^Set Comparison Method
			echo This property will decide the method/approach of comparing scores from reports.
			echo There are two options, Delta ^(meaning change^), and Standard Deviation.
			echo When choosing Delta, you have the option of using either percent change difference ^(threshold: percent_change^),
			echo ^or for more simple comparisons, the option to compare the difference of raw score ^(threshold: greater^). 
			echo Standard deviation comparisons are checked with a deviations allowed property, which will be required ^if testing for deviations.
			echo Standard Deviation comparisons become more useful the more reports you have and compare against.
			echo Valid options: delta, sd
			echo Example: delta
			echo:
			set /p method="method: "
			echo:
			if !method! == delta (
				echo ^Set Threshold
				echo When using the delta method ^for comparisons, you must specify whether you wish
				echo to compare the raw score difference ^(greater^), ^or ^if you want to compare with 
				echo percent change difference^(percent_change^). Greater threshold is more generic
				echo and simple, while percent_change can check for anomalies in both directions,
				echo meaning ^if you have a benchmark that does exponentially better from one report to the
				echo next, it will count as an anomaly. 
				echo Valid options: percent_change, greater
				echo Example: percent_change
				echo:
				set /p threshold="threshold: "
				echo:
				if !threshold! == percent_change (
					echo ^Set Percent Change Allowed
					echo When using the delta method of comparing, along with the percent change threshold,
					echo you must dictate the amount of percent change allowed. An accurate/beneficial value
					echo will depend on the scores of your reports, i.e., some comparisons may benefit from a lenient
					echo value such as 25^%, while other projects may require much tighter values, such as 3^%.
					echo Valid options: any positive number, ^% not necessary
					echo Example: 7.7
					echo:
					set /p percentChangeAllowed="percentChangeAllowed: "
					echo:
				)
			)
			if !method! == sd (
				echo ^Set Deviations Allowed
				echo Using the standard deviation comaprison method is better ^for analyzing regression
				echo over a long period of time, the more valid reports you have ^for your project,
				echo the more useful standard deviation comparisons become. When comparing
				echo via standard deviation, you must set the deviations allowed. A good deviations allowed
				echo value depeneds on the general score/difference between your reports.
				echo Standard deviation comparisons require at minimum 3 reports compared.
				echo Valid options: any number greater than 0
				echo Example: 1.5
				echo:
				set /p deviationsAllowed="deviationsAlllowed: "
				echo:
				)
			
			goto checkComparisonOverwrite
			)
		if [%sel%] == [7] (
			echo -----------------------------
			echo Generating blank ^(template^) cybench-automation.properties.
			echo Please remember to fill in values before running comparisons!
			echo -----------------------------
			
			goto checkComparisonOverwrite
			)			
		if [%sel%] == [8] (
			echo:
			echo Breakdown of options
			echo -----------------------------
			echo:
			echo 1. Modify Project Directory
			echo --- Correct project directory is necessary for the config files to be generated
			echo --- in the correct folder. ^If you run this script from your project's root directory,
			echo --- no modification is required. Otherwise, you will manually need to set this value
			echo --- to your project's root directory. You can also always manually move the config files
			echo --- ^if needed after generation.
			echo:
			echo 2. Generate Fully Custom CyBench Launcher properties file
			echo --- This option will prompt you for each valid configuration property
			echo --- in cybench-launcher.properties. A few of these values, such as additional JVM options,
			echo --- different java version, additional benchmark classes, etc. are not necessary, and are ^for
			echo --- more advanced users.
			echo:
			echo 3. Generate Basic Custom CyBench Launcher properties file ^(Recommended^)
			echo --- This option is best ^for first time users. You will be prompted ^for only required properties,
			echo --- such as tokens, email, whether you want to send the report, etc. JMH Settings are set by default,
			echo --- but can always be changed after generation.
			echo:
			echo 4. Generate Blank ^(template^) CyBench Launcher properties file
			echo --- Selecting this open will generate a blank cybench-launcher.properties, with all blank properties.
			echo --- This is useful ^if you wish to manually set properties, ^or ^if you require a clean slate.
			echo:
			echo 5. Generate Basic CyBench Automated Comparison properties file
			echo --- This option will generate a basic, fully functional automated comparison 
			echo --- properties file. The following configurations will be set: within version scope,
			echo --- only compare against the latest report, allow up to 2 anomalies, compare results/scores
			echo --- with a threshold ^for 15^% change, meaning all benchmark scores greater than 15^% difference,
			echo --- compared to the previous report, will result in an anomaly. Compare version and deviations allowed
			echo --- are included but are not necessary.
			echo:
			echo 6. Generate Fully Custom Automated Comparison properties file
			echo --- Selecting this option will guide you via prompts to creating a functional automated comparison.
			echo --- Each prompt will have information on valid options, and will not ask ^for unrelated properties,
			echo --- ^for example, selecting within ^for scope will not prompt you ^for a compare version.
			echo:
			echo 7. Generate Blank ^(template^) CyBench Automated Comparison properties file
			echo --- This option will generate a blank ^(all keys, but no values^) cybench-automation.properties file.
			echo --- This is useful ^if you wish to manually ^set comparison configuration properties, ^or ^if you require
			echo --- a clean slate.
			echo:
			echo 8. Display help/info ^for this tool
			echo --- Displays this information
			echo:
			echo 9. Exit
			echo --- Exit the script
			echo:
			set /p sel=Press Enter to return to the main menu...
			
			goto do
			)
		
			
		if [%sel%] == [9] (
			goto exit
			)
			
		echo Please select a valid option...
		
:doneModDir 
	set /p sel=Modified project and config directory, press Enter to continue...
	
	goto do

:checkOverwrite
	if exist "%configDir%cybench-launcher.properties" (
		echo WARNING: cybench-launcher.properties already exists. Continuing will overwrite the pre-existing properties file.
		echo Please make a backup if needed before continuing.
		set /p shouldOverwrite="Continue with overwrite? [y/n]: "
		if !shouldOverwrite! == y (
			goto generateConfigFile
			) else (
				set /p sel="Abandoned properties file overwrite, press Enter to continue..."
				goto do
		)
	) else (
	goto generateConfigFile)

:checkComparisonOverwrite
	if exist "%configDir%cybench-automation.properties" (
		echo WARNING: cybench-automation.properties already exists. Continuing will overwrite the pre-existing properties file.
		echo Please make a backup if needed before continuing.
		set /p shouldOverwriteComparison="Continue with overwrite? [y/n]: "
		if !shouldOverwriteComparison! == y (
			goto generateComparisonConfig
			) else {
				set /p sel="Abandoned properties file overwrite, press Enter to continue..."
				goto do
		)
	) else (
	goto generateComparisonConfig

:generateComparisonConfig
	echo:
	(
		echo #
		echo # Copyright ^(C^) 2020-2022, Nastel Technologies.
		echo #
		echo # This library is free software; you can redistribute it and/^or
		echo # modify it under the terms of the GNU Lesser General Public
		echo # License as published by the Free Software Foundation; either
		echo # version 2.1 of the License, ^or ^(at your option^) any later version.
		echo #
		echo # This library is distributed in the hope that it will be useful,
		echo # but WITHOUT ANY WARRANTY; without even the implied warranty of
		echo # MERCHANTABILITY ^or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
		echo # Lesser General Public License for more details.
		echo #
		echo # You should have received a copy of the GNU Lesser General Public
		echo # License along with this library; ^if not, write to the Free Software
		echo # Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
		echo #
		echo #
		echo:
		echo ## Options ^{within, between^}
		echo ### ^{within^} will compare all benchmarks within the benchmarked version
		echo ### ^if ^{between^} is chosen, must specify ^{compareVersion^} ^(will compare benchmarked version to the specified version^)
		echo ### add ^{compareVersion^} to specify which version to compare to 
		echo scope=%scope%
		echo:
		echo # Used only for BETWEEN ^(scope^) version comparisons
		echo compareVersion=%compareVersion%
		echo:
		echo # How many reports do you want to compare against^?
		echo # "1" will compare this report against the most recent report in the version you are comparing against
		echo ## "> 1" will compare this report against the average of the scores of the most recent # reports in the version you are comparing against
		echo numLatestReports=%numLatestReports%
		echo:
		echo # How many anomalies ^do you want to allow^?
		echo # ^If the number of benchmark anomalies surpasses your specified number, CyBench benchmark runner will fail... triggering your CI/CD pipeline to halt
		echo anomaliesAllowed=%anomaliesAllowed%
		echo:
		echo # method will be how the benchmarks will be compared against eachother
		echo ## Options ^{delta, SD^} 
		echo ### ^if ^{SD^} is chosen, must specify ^{deviationsAllowed^} 
		echo 	### ^{deviationsAllowed^} will be the amount of deviations you will allow your score to be away from the mean of the previous X values ^(X specified as ^{numLatestReports^}^)
		echo ### ^if ^{delta^} is chosen, must specify ^{threshold^} 
		echo 	# ^{threshold^} = how to specify a passing benchmark 
		echo 	## Options ^{percent_change, greater^} 
		echo 	### ^{greater^} will check to see ^if new score is simply greater than the compared to score 
		echo 	### if ^{percent_change^} is chosen, must specify ^{percentChangeAllowed^} 
        echo 		### ^{percentChangeAllowed^} = percentage score should be within in order to pass 
        echo 		### ex. 5% means the new score should be within 5% of the previous threshold 
		echo method=%method%
		echo threshold=%threshold%
		echo percentChangeAllowed=%percentChangeAllowed%
		echo deviationsAllowed=%deviationsAllowed%
	) > %configDir%cybench-automation.properties
	

	
	set /p sel=Generated cybench-automation.properties, press Enter to continue...
	
 goto do

:generateConfigFile
	echo:
	
	(
		echo #
		echo # Copyright ^(C^) 2020-2022, Nastel Technologies.
		echo #
		echo # This library is free software; you can redistribute it and/^or
		echo # modify it under the terms of the GNU Lesser General Public
		echo # License as published by the Free Software Foundation; either
		echo # version 2.1 of the License, ^or ^(at your option^) any later version.
		echo #
		echo # This library is distributed in the hope that it will be useful,
		echo # but WITHOUT ANY WARRANTY; without even the implied warranty of
		echo # MERCHANTABILITY ^or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
		echo # Lesser General Public License for more details.
		echo #
		echo # You should have received a copy of the GNU Lesser General Public
		echo # License along with this library; ^if not, write to the Free Software
		echo # Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
		echo #
		echo #
		
		echo ### Custom JVM options/properties
		echo javaOptions=%javaOptions%
		echo:
		echo ### Full path of alternative java.exe to be used during execution
		echo javaToUsePath=%javaToUsePath%
		echo:
		echo ### Full path of ^(alternative/extra^) .jar file^(s^) containing JMH Benchmarks.
		echo benchmarks=%benchmarks%
		echo:
		echo ### Provided tokens for accessing your private workspace, containing reports and comparisons
		echo ### benchAccessToken: Used for submitting reports after benchmark run completes
		echo ### benchQueryToken: Used for running automatic comparisons after benchmark run completes.
		echo benchAccessToken=%benchAccessToken%
		echo benchQueryToken=%benchQueryToken%
		echo:
		echo ### E-mail address to aid in associating uploaded reports with user.
		echo emailAddress=%emailAddress%
		echo:
		echo ### Decide if reports should be sent to CyBench UI automatically, if false, reports are still saved locally.
		if not !sendReport! == false (
			if not !sendReport! == f (
				if [%sendReport%] == [] (
					echo sendReport=
					) else (
						echo sendReport=true
					) 
				) else echo sendReport=false		
			) else echo sendReport=false
		echo:
		echo ### If sending report to CyBench UI, decide if they should post to your private workspace ^(private^) ^or CyBench's public repo ^(^not recommended^)
		if not %reportUploadStatus% == public (
			if not %reportUploadStatus% == pub (
				if [%reportUploadStatus%] == [] (
					echo reportUploadStatus=
					) else (
					echo reportUploadStatus=private
					)
				) else echo reportUploadStatus=public
			) else echo reportUploadStatus=public
		echo:
		echo ### Choose a report name to be reflected on CyBench's UI
		echo reportName=%reportName%
		echo:
		echo ### Specifcy benchmark classes to run, if your project contains multiple and you only wish to execute certain ones
		echo ### Multiple classes must be designated with their fully qualified name, comma seperated
		echo benchmarkClasses=%benchmarkClasses%
		echo:
		echo ### Benchmarking execution configuration ^(JMH specific settings^)
		echo:
		echo ### Number of full executions of a benchmark ^(forks^), both warmup and measurement iterations
		echo numberOfBenchmarkForks=%numberOfBenchmarkForks%
		echo ### Number of measurement iterations, per benchmark operation ^(how many times should the benchmark method execute^)
		echo measurementIterations=%measurementIterations%
		echo ### Number of seconds dedicated for each measurement iterations
		echo measurementSeconds=%measurementSeconds%
		echo ### Number of warmup iterations. Warmup iterations don't affect final score, but help to warm up the JVM
		echo warmupIterations=%warmupIterations%
		echo ### Number of seconds dedicated for each warmup iteration
		echo warmupSeconds=%warmupSeconds%
		echo ### Number of threads allocated for benchmark execution
		echo runThreadCount=%runThreadCount%
		echo ### Benchmark modes to run
		echo ### For more info on various benchmark modes, visit https://blog.avenuecode.com/java-microbenchmarks-with-jmh-part-2
		echo benchmarkModes=%benchmarkModes%
		echo ### Additional JMH command line arguements
		echo ### Extra JMH properties can be configured/^set.
		echo ### For an overview of additional flags and properties, visit https://github.com/guozheng/jmh-tutorial/blob/master/README.md#jmh-command-line-options
		echo jmhArguments=%jmhArguments%
	) > %configDir%cybench-launcher.properties
		
	set /p sel=Generated cybench-launcher.properties, press Enter to continue...
	
 goto do

:exit