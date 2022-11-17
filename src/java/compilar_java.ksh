#!/bin/ksh

PROC_CLASSPATH=":.:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso2/lib/*"
OUT="/amxusers3/test/oms/chioms4/oms/application/LanzaProceso2/classes"


echo javac -cp ${CLASS_PATH}:${PROC_CLASSPATH}
javac -Xlint:deprecation -cp ${CLASS_PATH}:${PROC_CLASSPATH} -d ${OUT} $(find . -name "*.java")
