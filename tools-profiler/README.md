Using the profiler :

set the file to test in src/main/Resources/ontologiestotest
open src/test/java/openllet.profiler.statistical/ReleasePerformanceTest.java and comment the "@Ignore" line

Prepare your computer for a benchmark :
run the main of src/test/java/openllet.profiler.statistical/ReleasePerformanceTestSuite.java

the result are put in src/main/resources/trys
rename the new file. (version & cpu infos)

Compare the results with previous by runnning the main of src/main/java/openllet/profiler.statistical/ReleasePerformanceVisualize.java

