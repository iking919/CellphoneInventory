#!/bin/bash

# This script compiles and runs the Java program. It assumes that you have a "src" folder containing your Java source files, and it will compile them into an "out" folder.
# Usage:  ./run.sh [arguments]
set -e 

# Compile everything inside src into an out folder
javac -sourcepath src -d out src/Main.java

# Run Main and forward any command-line arguments ("$@") you pass to the script
java -cp out Main "$@"
