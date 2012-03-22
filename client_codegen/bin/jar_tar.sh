DOT_VERSION=0
jar -cf ../../indivo_client_java-1.$DOT_VERSION.jar -C ../classes org

if 
  [ -e "indivo_client_java-1.$DOT_VERSION" ]
then
  rm -r indivo_client_java-1.$DOT_VERSION
fi
mkdir indivo_client_java-1.$DOT_VERSION
mkdir indivo_client_java-1.$DOT_VERSION/classes
cp ../../classes/log4j.xml indivo_client_java-1.$DOT_VERSION/classes
cp ../../indivo_client_java-1.$DOT_VERSION.jar indivo_client_java-1.$DOT_VERSION
cp -r ../../javadoc indivo_client_java-1.$DOT_VERSION
cp -r ../../lib indivo_client_java-1.$DOT_VERSION
cp ../src/README.html indivo_client_java-1.$DOT_VERSION
cp ../../NOTICE.txt indivo_client_java-1.$DOT_VERSION
cp -r ../../examples indivo_client_java-1.$DOT_VERSION

tar -cf ../../indivo_client_java-1.$DOT_VERSION.tar indivo_client_java-1.$DOT_VERSION
