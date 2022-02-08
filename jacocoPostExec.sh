PATH_JACOCO_CLI_JAR="src/test/lib"
PATH_JCS_SRC="src/test"
PATH_JCS_JAR="src/test/lib/jcs-1.3.jar"
PATH_JCS_FAT_JAR="target/fatjar"
PATH_COVERAGE_TARGET="target/jacoco-gen/jcs-coverage/"

echo "*** JaCoCo fat-jar instrumentation ***"
echo "Instrumenting..."

java -jar ${PATH_JACOCO_CLI_JAR}/jacococli.jar instrument ${PATH_JCS_JAR} --dest ${PATH_JCS_FAT_JAR}

echo "*** JaCoCo report generation ***"
echo -n "Making jcs-coverage directory... "
mkdir -p target/jacoco-gen/jcs-coverage/
echo "Done."

echo "Generating Jacoco reports..."
java -jar ${PATH_JACOCO_CLI_JAR}/jacococli.jar report target/jacoco.exec \
          --classfiles  ${PATH_JCS_JAR}                 \
          --sourcefiles ${PATH_JCS_SRC}                 \
          --html        ${PATH_COVERAGE_TARGET}         \
          --xml         ${PATH_COVERAGE_TARGET}file.xml \
          --csv         ${PATH_COVERAGE_TARGET}file.csv
echo "Done."
