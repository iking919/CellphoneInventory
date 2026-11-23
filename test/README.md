JUnit test files for testing phone inventory backend.

The JUnit testing covers phone-field validation, filter validation and matching, ascending result ordering, parser behavior for invalid and unreadable inventory files, and command-line filtering and error-reporting.

In Git bash, the tests are runnable with:

```bash
bash run-tests.sh
```

On Windows PowerShell, the tests are runnable with:

```powershell
$sources = Get-ChildItem src,test -Filter *.java -Recurse | ForEach-Object { $_.FullName }
javac -cp 'lib\junit-platform-console-standalone-1.11.4.jar' -d test $sources
java -jar 'lib\junit-platform-console-standalone-1.11.4.jar' execute --class-path test --scan-class-path
```

Alternatively, run individual test file (TESTFILE.java) with:

```powershell
javac -cp "lib/junit-platform-console-standalone-1.11.4.jar;src;test" -d test src/Phone.java test/PhoneTest.java

java -jar lib/junit-platform-console-standalone-1.11.4.jar execute --class-path test --select-class PhoneTest
```