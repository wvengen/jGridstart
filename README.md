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

  * __mvn-parent__ -
      Maven parent project with common configuration

  * __osutils__ (`nl.nikhef.jgridstart.osutils`) -
      General utilities for interfacing with the operating system. Java is
      not very good in providing complete integration with the underlying
      operating system, like file permissions and process spawning.

  * __browsers__ (`nl.nikhef.browsers`) -
      Interfacing web browsers installed on the user's system (Linux,
      Windows, Mac OS X). Discovery of default and installed browsers,
      opening web pages and installing PKCS#12 files.

  * __passwordcache__ (`nl.nikhef.jgridstart.passwordcache`) -
      Cache for user-entered passwords to avoid having to type them again
      and again (with timeout). Integration with [PEMReader] and [PEMWriter].

  * __xhtmlrenderer__ (`nl.nikhef.xhtmlrenderer.swing`) -
      Templates and enhancements for the [Flying Saucer XHTMLRenderer].

  * __jgridstart-main__ (`nl.nikhef.jgridstart`) -
      Main jGridstart application.

  * __jgridstart-ca-dutchgrid__ (`nl.nikhef.jgridstart.ca.dutchgrid`) -
      [DutchGrid] CA package, providing an interface to the DutchGrid authority
      to submit and retrieve certificates, as well as its request wizard.

  * __jgridstart-ca-confusa__ (`nl.nikhef.jgridstart.ca.confusa`) -
      [Confusa] CA package, providing an OAuth-based interaction with the
      Confusa certificate authority to submit and retrieve certificates, as well
      as the corresponding request wizard.

  * __jgridstart-small__ -
      Creation of a minified jar using [ProGuard]. Currently this is also the place
      where the included ca packages are selected.

  * __jgridstart-wrapper__ -
      Wrapper around jgridstart-small that contains the minified jar as well
      as bouncycastle. When it is run, it unpacks them to a temporary directory
      and executes it locally. This is to avoid all kinds of problems when
      using bouncycastle and java web start together.

  * __jgridstart-jws__ -
      Java Web Start package.

  * __jgridstart-tests__ -
      Bundle of combined tests of all packages, with dependencies. This is
      useful for running the tests at different places. Contains a test
      runner class `nl.nikhef.AllTests` (needs work on diagnostics output).

  * __testrunner__ -
      GUI for running unit tests on user's computer to analyse problems.
      Currently unusable, needs to be adapted.

  * __testca__ -
      A simple online certificate authority for testing jGridstart. This is
      used to provide a way to test the user-interaction with an online
      certificate authority. jGridstart's `nl.nikhef.jgridstart.ca.TestCA`
      is the corresponding CA provider.
      Please see `testca/README.md` for more information.


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

2. _Selection of a CA interface (ca-specific module)_

     Most probably you'll need to implement the Java interface for talking
     to your certificate authority. You can copy one of the existing ca
     modules, and adapt it for your situation. Please use a package name
     that reflects your domain (e.g. `org.example.jgridstart.ca`).
     You can also look at jGridstart's TestCA and LocalCA as examples.
     Then add this module as a dependency to the jgridstart-wrapper module.

3. _Customization of the request wizard (ca-specific module)_

     Each certificate authority's process is slightly different, that's why
     you'd write your own request wizard process. You could start off from
     jgridstart-ca-dutchgrid, which has an off-line CA process.

     The contents of the wizard's pages are typically present in files named
     `requestwizard-xx.html`. These are added as pages to a subclass of
     `nl.nikhef.jgridstart.gui.wizard.RequestWizardCommon`. Please see the
     existing CA's as examples of usage details.

4. _Specification of organisations for which one can signup (jgridstart module)_

    In `jgridstart/src/main/resources/conf/cert_signup.conf`, see comments.
    At the moment one also needs to update `CertificateRequest#postFillData`
    (in package `nl.nikhef.jgridstart`) and specify how to create a full
    DN from user information.

With this adapted, you'll need to build your own jGridstart distribution (as
explained above). Then sign it using a commercial code-signing certificate.
When you contribute your module, we can include it and add it to the official
jGridstart releases.


License
-------

The jGridstart source code is fully open source under the [Apache 2.0 License].


[DutchGrid]: http://ca.dutchgrid.nl/
[PEMReader]: http://www.bouncycastle.org/docs/pkixdocs1.4/org/bouncycastle/openssl/PEMReader.html
[PEMWriter]: http://www.bouncycastle.org/docs/pkixdocs1.4/org/bouncycastle/openssl/PEMWriter.html
[Flying Saucer XHTMLRenderer]: http://code.google.com/p/flying-saucer/
[ProGuard]: http://proguard.sf.net/
[Maven]: http://maven.apache.org/
[Eclipse]: http://www.eclipse.org/
[m2eclipse]: http://www.eclipse.org/m2e/
[Apache 2.0 License]: http://www.apache.org/licenses/LICENSE-2.0

