HACKING
=======

Coding Conventions
------------------

Checkstyle is used to ensure an overall good code quality.
Please use *tools/checkstyle.xml* to check your code before publishing it.
This helps others to understand your code and it helps you to find mistakes.

To document your code, you should use Javadoc. Write short comments,
but explain, what the things are doing, so everybody understands how to
use your code. Do not describe how the code is working.
To ensure other people can work on your code later, you should also document
private members and write short non-Javadoc comments in your methods.

If you use `TODO` comments in your code and it is a task for other members of
development team too, you should also write a ticket for this.


Local Maven Repository
----------------------

If you have an dependency that you want to use in *pom.xml*, but it is not
registered in the global maven repository, you can install a jar file of the
library in the local project repository using the following command:

    mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
        -Dfile=<path-to-file> -DgroupId=<groupId> -DartifactId=<artifactId> \
        -Dversion=<version> -Dpackaging=jar -DlocalRepositoryPath=repo \
        -DcreateChecksum=true

After installing it there, you can use it in pom.xml like other global
registered plugins and libraries.


Debugging
---------

For debugging, helper scripts from tools folder can be used with your favorite
IDE and the remote debugger protocol. Run the scripts from project folder.
