jar -cf ../../indivo_client_java-1.0.jar -C ../classes org

if 
  [ -e "indivo_client_java-1.0" ]
then
  rm -r indivo_client_java-1.0
fi
mkdir indivo_client_java-1.0
cp ../../indivo_client_java-1.0.jar indivo_client_java-1.0
cp -r ../../javadoc indivo_client_java-1.0
cp -r ../../lib indivo_client_java-1.0
cp ../src/README.txt indivo_client_java-1.0
cp ../../NOTICE.txt indivo_client_java-1.0
cp -r ../../src/main/java/examples indivo_client_java-1.0

tar -cf ../../indivo_client_java-1.0.tar indivo_client_java-1.0
#tar -cf ../../indivo_client_java-1.0.tar -C ../.. README NOTICE.txt indivo_client_java-1.0.jar  javadoc  lib  src client_codegen
