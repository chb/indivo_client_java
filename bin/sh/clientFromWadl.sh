java -cp \
../../../target/client_codegen-0.4.jar:\
../../../target/dependency/gson-1.6.jar \
org.indivo.client.codegen.GenClientFromWadl  $1 ../forCodeGen/Rest_SHELL.java   ../../../build
