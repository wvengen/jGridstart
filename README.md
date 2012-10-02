jGridstart
==========

[![Build Status](https://secure.travis-ci.org/wvengen/jGridstart.png)](http://travis-ci.org/wvengen/jGridstart)

jGridstart is a graphical user interface in Java that helps grid end-users to
request, obtain, install and renew grid certificates in a friendly way.

Grid certificate authorities (who issue certificates) can use it to provide a
friendly interface to their services. It is currently tailored for the
[DutchGrid] certificate authority. While it is possible to use it for your own
certificate authority (see below), it would be possible to make this a little
smoother; if enough interest is expressed I'll look into that.

For more information, please visit http://jgridstart.nikhef.nl/


Source code layout
------------------

The source is split into several modules.

  * _mvn-parent_

      Maven parent project with common configuration

  * _osutils_ (`nl.nikhef.jgridstart.osutils`)

      General utilities for interfacing with the operating system. Java is
      not very good in providing complete integration with the underlying
      operating system, like file permissions and process spawning.

  * _browsers_ (`nl.nikhef.browsers`)

      Interfacing web browsers installed on the user's system (Linux,
      Windows, Mac OS X). Discovery of default and installed browsers,
      opening web pages and installing PKCS#12 files.

  * _passwordcache_ (`nl.nikhef.jgridstart.passwordcache`)

      Cache for user-entered passwords to avoid having to type them again
      and again (with timeout). Integration with [PEMReader] and [PEMWriter].

  * _xhtmlrenderer_ (`nl.nikhef.xhtmlrenderer.swing`)

      Templates and enhancements for the [Flying Saucer XHTMLRenderer].

  * _jgridstart-main_ (`nl.nikhef.jgridstart`)

      Main jGridstart application.

  * _jgridstart-small_

      Creation of a minified jar using [ProGuard].

  * _jgridstart-wrapper_

      Wrapper around jgridstart-small that contains the minified jar as well
      as bouncycastle. When it is run, it unpacks them to a temporary directory
      and executes it locally. This is to avoid all kinds of problems when
      using bouncycastle and java web start together.

  * _jgridstart-jws_

      Java Web Start package.

  * _jgridstart-tests_

      Bundle of combined tests of all packages, with dependencies. This is
      useful for running the tests at different places. Contains a test
      runner class `nl.nikhef.AllTests` (needs work on diagnostics output).

  * _testrunner_

      GUI for running unit tests on user's computer to analyse problems.
      Currently unusable, needs to be adapted.


Compiling & developing
----------------------

To build the jGridstart application, invoke [Maven]:

    mvn package

This wil compile all modules (except testrunner and jgridstart-tests, as they
are not completely finished not required to use jGridstart), run tests, and
create packages. The end result would usually be
`jgridstart-wrapper/target/jgridstart-wrapper-x.y.jar`. This jar is
self-contained and can be run directly from the command-line using

    java -jar jgridstart-wrapper/target/jgridstart-wrapper-[0-9.]*.jar

To use Java Web Start for running jGridstart directly from a website, the
directory `jgridstart-jws/target/jnlp` contains everything required to do so.
Since JNLP files contain an absolute base URL, one needs to set these using
`deploy.sh` in the same directory.

During development it may be useful to run jGridstart without packaging it.
`jgridstart-main` contains a shellscript `jgridstart.sh` (and batch file
`jgridstart.bat` for Windows) to do that. Giving it the command-line argument
`-screenshots <some_dir>` will run jGridstart in an automated mode, taking
screenshots at various places.


To use this in the [Eclipse] integrated development environment (with
[m2eclipse]), I'd suggest to create a new workspace, and then select `File`,
`Import...` and choose `Maven`, `Existing Maven projects...`. Select the folder
containing this `README.md` as `Root Directory`, and import all modules as
projects.


jGridstart for Certificate Authorities
--------------------------------------

Adapting jGridstart for a new certificate authority requires:

1. _Modification of the configuration_

     in `jgridstart/src/main/resources/resources/conf/global.properties`.
     See comments in the file. Note that when run as a java web start
     application, properties can be overridden in the jnlp file
     (optionally prefixed with `jnlp.` to avoid a security warning).

2. _Selection of a CA interface (module jgridstart)_

     The `nl.nikhef.jgridstart.ca` package contains the interface CA, which
     provides an interface between jGridstart and the CA. The specific
     implementation used is specified in the global configuration file.
     You probably need to write one tailored to your CA interface. Please
     see DutchGridCA, TestCA and LocalCA for examples. Please send us your
     implementation so we can add it to jGridstart.

3. _Customization of the request wizard (module jgridstart)_

     `RequestWizard` (in package `nl.nikhef.jgridstart.gui`) contains the
     user-interface logic for requesting/renew certificates. The contents
     of the wizard's pages are present in the files `requestwizard-xx.html`.
     Currently, one would need to rewrite RequestWizard for your specific
     request process. In the future this should become more easily
     customizable.

4. _Specification of organisations for which one can signup_

    In `jgridstart/src/main/resources/conf/cert_signup.conf`, see comments.
    At the moment one also needs to update `CertificateRequest#postFillData`
    (in package `nl.nikhef.jgridstart`) and specify how to create a full
    DN from user information. This is scheduled for improvement as well.

5. _Signing of the resulting JAR_

    This is required for running jGridstart as a java web start application.
    By default a temporary generated key is used. For production you may
    want to use a commercial code-signing certificate.


[DutchGrid]: http://ca.dutchgrid.nl/
[PEMReader]: http://www.bouncycastle.org/docs/pkixdocs1.4/org/bouncycastle/openssl/PEMReader.html
[PEMWriter]: http://www.bouncycastle.org/docs/pkixdocs1.4/org/bouncycastle/openssl/PEMWriter.html
[Flying Saucer XHTMLRenderer]: http://code.google.com/p/flying-saucer/
[ProGuard]: http://proguard.sf.net/
[Maven]: http://maven.apache.org/
[Eclipse]: http://www.eclipse.org/
[m2eclipse]: http://www.eclipse.org/m2e/

