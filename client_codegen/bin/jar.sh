if 
  [ -e "../classes" ]
then
  rm -r ../classes
fi

mkdir ../classes

javac -d ../classes -classpath \
../../lib/commons-codec-1.3.jar:\
../../lib/commons-lang-2.5.jar:\
../../lib/httpclient-4.0.1.jar:\
../../lib/httpcore-4.0.1.jar:\
../../lib/signpost-core-1.2.jar:\
../../lib/signpost-commonshttp4-1.2.jar:\
../../lib/gson-1.6.jar:\
../../lib/commons-logging-1.1.1.jar \
   ../../src/main/java/org/indivo/client/Rest.java \
   ../../src/main/java/org/indivo/client/Utils.java \
   ../../src/main/java/org/indivo/client/IndivoClientException.java \
   ../../src/main/java/org/indivo/client/IndivoClientExceptionHttp404.java \
   ../../src/main/java/org/indivo/client/IndivoClientConnectException.java \
   ../../src/main/java/org/indivo/client/DefaultResponseTypeConversion.java \
   ../../src/main/java/org/indivo/client/ResponseTypeConversion.java

jar -cf ../../indivo_client_java-1.0.jar -C ../classes org
tar -cf ../../indivo_client_java-1.0.tar -C ../.. README NOTICE.txt indivo_client_java-1.0.jar  javadoc  lib  src client_codegen
