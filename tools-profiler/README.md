Using the profiler :

1) set the file to test in src/main/resources/ontologiestotest

2) open src/test/java/openllet.profiler.statistical/ReleasePerformanceTest.java and comment the "@Ignore" line

3) Prepare your computer for a benchmark

4) run the main of src/test/java/openllet.profiler.statistical/ReleasePerformanceTestSuite.java

5) open src/test/java/openllet.profiler.statistical/ReleasePerformanceTest.java and re-comment the "@Ignore" line


the result are put in src/main/resources/trys
rename the new file. (version & cpu infos)

Compare the results with previous by runnning the main of src/main/java/openllet/profiler.statistical/ReleasePerformanceVisualize.java

