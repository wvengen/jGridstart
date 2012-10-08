Creating a jGridstart release
=============================

When, after fixing bugs and adding new features, you'd like to bring those
changes to the general public, it is time to make a new release of jGridstart.
This would involve the following steps:


1. *Make sure that everything works.*
   When you've made changes that might involve platform-specific behaviour,
   make sure to test affected functionalities on the major platforms (Linux,
   Mac OS X and Windows). Please see `jgridstart-tests/README.md` for more
   information on that.


2. *Update version numbers.*
   Consider what modules have changed. For those modules, update the version
   number. Also update the version number where these are used as dependencies.

   If you just changed `jgridstart-main`, you can run the following commands
   (using [xmlstarlet]) to update the relevant modules to version x.y:

        VERSION=x.y
        xmlstarlet ed -P -L -N m=http://maven.apache.org/POM/4.0.0 \
            -u "/m:project/m:version" -v "$VERSION" \
            -u "/m:project/m:dependencies/m:dependency[child::m:groupId='nl.nikhef.jgridstart' \
                        and starts-with(child::m:artifactId,'jgridstart-')]/m:version" -v "$VERSION" \
            -u "/descendant::m:artifactItems/m:artifactItem[child::m:groupId='nl.nikhef.jgridstart' \
                        and starts-with(child::m:artifactId,'jgridstart-')]/m:version" -v "$VERSION" \
            jgridstart-*/pom.xml
        sed -si '/^<?xml.*$/d; s/\( xsi:schemaLocation\)/\n \1/' jgridstart-*/pom.xml

   To view the version numbers of all modules, you use the following command:
   
       xmlstarlet sel -N m=http://maven.apache.org/POM/4.0.0 -t -f -o ' ' -v /m:project/m:version */pom.xml


3. *Create final version*
   by running `mvn clean install` from the project's root.


4. *Sign resulting JAR*
   using a commercial code-signing certificate, so that users know they are
   relatively safe running the code. This is important, since jGridstart
   requires full access to the system and won't work without a signed JAR.
   You can use [jarsigner]:

       jarsigner -keystore /my/codecert.jks jgridstart-jws/target/jnlp/jgridstart-wrapper-x.y.jar store_entry_name

   Now `jgridstart-wrapper-x.y.jar` is ready for deployment.


5. *Upload release.*
   Each release is uploaded to http://jgridstart.nikhef.nl/release/x.y .
   Update the URL locations in the JNLP so that it works from there:

       cd jgridstart-jws/target/jnlp
       sh deploy.sh http://jgridstart.nikhef.nl/release/x.y

   Then copy all files found in `jgridstart-jws/target/jnlp` to the location.


6. *Publish javadoc.*
   TODO: This is something that still needs to be put in place.


7. *Update the Wiki.*
   http://jgridstart.nikhef.nl/Releases contains a list of jGridstart's
   releases. Add a new entry, make sure the links point to the correct release
   location; review source code history and add changes relevant for users and
   CAs (with bug links when present).
   Also update http://jgridstart.nikhef.nl/Test to point to the new release.


8. *Commit and create tag* so that the source code reflects the release as well.

        git commit -m 'release x.y'
        git tag jgridstart_x.y
        git push --tags


9. *Prepare for Certificate Authority.* The DutchGrid CA uses jGridstart
   directly, and we prepare the release for them a little more. Just run the
   script `copyrelease.sh x.y` in jgridstart.nikhef.nl's `/ca` directory. Then
   you're ready to mail them!



It would be nice to eventually use the [Maven release plugin] to automate a lot of this.
For now, stick to this and you're fine.
And finally: please feel free to update and improve this file!


[xmlstarlet]: http://xmlstar.sourceforge.net/
[jarsigner]: http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/jarsigner.html
[Maven release plugin]: http://maven.apache.org/plugins/maven-release-plugin
