README
======

Requirements
------------

Software:
 * JDK 1.6.0_18
 * Maven 2.2.1

Hardware:
 * Internet access for Maven to download plugins and dependencies
 * 500 MB RAM
 * 1GB hard disc space

 
Build
-----

Cleanup the workspace and build the package:

    mvn clean package

Testing using JUnit and Cobertura will be done in package step.
You will find the results at *target/surefire-reports*.
If you want to skip tests (not recommended),
you can use the following command to compile instead the one shown above:

    mvn -Dmaven.test.skip=true clean package

If you need javadoc files:

    mvn javadoc:javadoc

Then, you will find Javadocs in *target/site/apidocs/docs*


Install
-------

After compilation, you will find an executable jar file under
*target/bsv-X.X.X.jar*
Because this file is portable, you can copy it together with the target/lib
folder to any location on your computer. Depending on your operation system
and desktop environment, you can start it via double clicking or with the
following command:

    java -jar path/to/bsv-X.X.X.jar
