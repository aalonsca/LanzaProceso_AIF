#!/bin/ksh

PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsOrderingCihDataTypes.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsProcMgrBase.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsProcMgrEngine.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/ClfyCore.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/core_domain.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/ofc.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/ojdbc6.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:.:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/wlthint3client.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsCihDatatypes.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsSvcCommon.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsProcEmitted.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsProcMgrEngineExample.jar;
PROC_CLASSPATH=$PROC_CLASSPATH:/amxusers3/test/oms/chioms4/oms/application/LanzaProceso/lib/AmdocsCore.jar;
export PROC_CLASSPATH

echo javac -Xlint:unchecked -classpath $PROC_CLASSPATH $1
javac -Xlint:unchecked -classpath $PROC_CLASSPATH $1
