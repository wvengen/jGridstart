jGridstart Tests
================

In earlier versions of jGridstart, there once was a GUI testing application
which would run all unit tests and upload the results. This has helped
enormously in debugging platform-specific problems. And there is quite some
platform-specific behaviour, for example in the `osutils` module.

With the move to the [Maven] build system, this functionality has not been
fully restored. There has been little incentive, since most of those issues
have been solved. Nevertheless, it is still useful to be able to easily run the
full test suite on a given computer.


Test-related modules
--------------------

In this maven-ised build, two modules are related to tests. `jgridstart-tests`
gathers all test classes (the parent POM, in module `mvn-parent`, specifies
that test JARs are built by default) with dependencies in a single JAR. This is
different from `jgridstart-small`, which omits test classes and their
dependencies to minimise the filesize.

At this moment, it is possible to run all tests from the command-line. Before
running the following commands, you need to have run `mvn install` on the
parent project (top directory) so that jGridstart is available in the local
Maven repository (usually `~/.m2`). From this directory, run:

    mvn package
    # using the JUnit test runner (recommended, for more output)
    java -cp target/jgridstart-tests-x.y.jar org.junit.runner.JUnitCore nl.nikhef.AllTests
    # or directly, if you insist
    java -jar target/jgridstart-tests-x.y.jar 

This will run console tests, graphical user-interface tests, and the full
graphical walkthrough simulation (same as automated screenshots generation).

To run these tests on a different location, where no Maven may be present, one
needs to have both the jgridstart-tests JAR and the BouncyCastle provider JAR
present.


Running tests using Maven
-------------------------

It is also possible to run tests using Maven directly, mainly useful during
development. By default, only console tests are being executed. One reason is
that they take a lot of time, another that they require full control over the
user's graphical interface. Also, results from running graphical user-interface
tests (including the walkthrough test) are often not fully reproducable. Timing
(including Java runtime cache behaviour) and window focus issues are likely to
be at work here (this is the reason why they are not enabled in the [Travis-CI]
continuous integration tests; it should technically be possible, however).

To just run the console tests, run:

    mvn test

To run the graphical tests as well, enable the `guiTests` profile (as defined in
module `mvn-parent`):

    mvn -DguiTests test

Using your graphical user-interface (like moving the mouse or pressing keys on
the keyboard) will affect testing, so please wait until they're done.


Automated screenshots test
--------------------------

The class `nl.nikhef.jgridstart.gui.util.ScreenshotsGuitest` contains a test
and main program to test the complete program flow, from creating a new
request, to renewal and import/export. This test works on a temporary `.globus`
directory and so does not affect the user's data. This class can be run
directly from `jgridstart-main`'s `jgridstart.sh` (or `jgridstart.bat` on
Windows) supplying it the `-screenshots screenshot_dir` arguments.



[Maven]: http://maven.apache.org/
[Travis-CI]: http://travis-ci.org/wvengen/jGridstart

