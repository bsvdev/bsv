#!/bin/sh

PORT=5005
TRANSPORT=dt_socket

echo "Start remote debugger server on port $PORT."
echo "Use your favorite IDE to connect/attach to it."
echo "Server uses $TRANSPORT transport."
echo "Shutdown server using CTRL-C."
echo

echo "==========SERVER OUTPUT=========="
java -Xdebug -Xrunjdwp:transport=$TRANSPORT,server=y,address=$PORT,suspend=y -jar target/bsv-1.0.1.jar
echo "================================="

echo
echo "Server is dead now"
