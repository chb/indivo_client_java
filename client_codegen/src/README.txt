This is the README file for the binary distribution of indivo_client_java-1.x in its .tar file.

tar -xf indivo_client_java-1.x

indivo_client_java-1.x.jar contains the classes an Indivo client application written in Java might use.
Writing http code to access the Indivo REST interface directly is an alternative.  We think using
the classes in indivo_client_java-1.x.jar is more convenient.

point your browser to file:///..../javadoc/index.xml in the javadoc directory

org.indivo.client.Rest has the public methods a client application will typically use.
method names in org.indivo.client.Rest are based on the equivalent REST URL-s.
org.indivo.client.Rest_py_client_style is identical with org.indivo.client.Rest except
that method names are the same as in the Python client.


indivo_client_java depends on some 3rd party .jar files, all included in this distribution, in the lib directory.
see NOTICES.txt for 3rd party jar license info.

Source distribution for indivo_client_java: [you must have git installed]
git clone git://github.com/chb/indivo_client_java.git

