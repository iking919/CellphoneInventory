
#!/bin/bash

# Complete bash script to run all JUnit tests found in test/, with downloaded jar file in lib. 
# Run with: "bash run-tests.sh"

# Define JUnit standalone library
JUNIT="lib/junit-platform-console-standalone-1.11.4.jar"

echo " Compiling Cellphone Inventory.."

# Remove previously compiled test classes
rm -rf test/*.class

# Compile application and test classes
javac -cp "$JUNIT" -d test src/*.java test/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed."
    exit 1
fi


echo " Running JUnit Tests..."

# Discover and execute all tests
java -jar "$JUNIT" execute \
    --class-path test \
    --scan-class-path

# Return JUnit's exit status
exit $?
