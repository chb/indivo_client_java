to create indivo_client_java-0.4.jar in ./target directory:
mvn install

to create javadoc in ./target/site/apidocs directory:
mvn javadoc:javadoc

before running command-line tests in src/test/sh (testAdmin.sh testChrome.sh and testPha.sh):
mvn dependency:copy-dependencies

to generate oauth dance testing servlet "indivoTestServlet.war" in testServlet/target:
cd testServlet
vi src/main/webapp/WEB-INF/web.xml
      modify <param-value>-s as needed
mvn install

client_codegen contains code used to generate Rest.java from indivo_wadl.xml.
client_codegen is not generally of interest to users, but look if you are curious.
