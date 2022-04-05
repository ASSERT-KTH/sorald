# This script executes Sorald in segment mode with a very limited amount of heap memory in order to verify that there are no
# memory leaks. As an example, if the CompilationUnitCollector is not cleared after each write, then all segments are
# held in memory for as long as Sorald executes (see https://github.com/SpoonLabs/sorald/issues/437).
#
# This script assumes that Sorald is already packaged to the default location, and that it is executed from the root
# of the repository.

set -o errexit
set -o nounset
set -o pipefail

SORALD_JAR_PATH=$(echo sorald/target/sorald-*-jar-with-dependencies.jar)
if [[ ! (-f "$SORALD_JAR_PATH") ]]; then
  echo "expected Sorald jar at $SORALD_JAR_PATH"
  exit 1
fi

HEAP_SIZE="128m"
MAX_FILES_PER_SEGMENT=25
SORALD_STDOUT_FILE="sorald_stdout.txt"
EXPECTED_NUM_FIXES_LINE="SerializableFieldInSerializableClassProcessor: 170"

echo "Running memory-constrained test with heap size=$HEAP_SIZE and segment size=$MAX_FILES_PER_SEGMENT"

git clone https://github.com/eclipse/eclipse-collections.git --depth 1 --branch 11.0.0.M1 --single-branch
java -Xmx"$HEAP_SIZE" -jar "$SORALD_JAR_PATH" repair --source eclipse-collections/eclipse-collections --rule-key 1948 \
    --repair-strategy SEGMENT \
    --max-files-per-segment $MAX_FILES_PER_SEGMENT > "$SORALD_STDOUT_FILE"

grep "$EXPECTED_NUM_FIXES_LINE" "$SORALD_STDOUT_FILE" || {
  echo "Did not find the expected amount of fixes line: $EXPECTED_NUM_FIXES_LINE"
  tail -n 10 $SORALD_STDOUT_FILE
  exit 1
}

echo "Memory-constrained test passed without error"
