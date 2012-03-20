javac -Xlint:unchecked  -source 1.6 -target 1.6  \
 -cp \
../indivo_client_java-1.RC1.jar:\
../lib/commons-codec-1.3.jar:\
../lib/commons-lang-2.5.jar:\
../lib/commons-logging-1.1.1.jar:\
../lib/gson-1.6.jar:\
../lib/httpclient-4.0.1.jar:\
../lib/httpcore-4.0.1.jar:\
../lib/javaee-web-api-6.0.jar:\
../lib/junit-3.8.1.jar:\
../lib/log4j-1.2.16.jar:\
../lib/signpost-commonshttp4-1.2.jar:\
../lib/signpost-core-1.2.jar \
   src/examples/TestClientServlet.java

if
  [ -e "WEB-INF" ]
then
  rm -r WEB-INF
fi
mkdir WEB-INF
mkdir WEB-INF/classes
mkdir WEB-INF/lib
cp -r src/examples WEB-INF/classes
cp ../classes/log4j.xml WEB-INF/classes
cp ../lib/*.jar WEB-INF/lib
cp src/web.xml WEB-INF
jar -cf TestClientServlet.war \
WEB-INF

