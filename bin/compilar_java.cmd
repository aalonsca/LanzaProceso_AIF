@echo off

SET PROC_CLASSPATH=.;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsOrderingCihDataTypes.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsProcMgrBase.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsProcMgrEngine.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\ClfyCore.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\core_domain.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\ofc.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\ojdbc6.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\wlthint3client.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsCihDatatypes.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsSvcCommon.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsProcEmitted.jar
SET PROC_CLASSPATH=%PROC_CLASSPATH%;C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsProcMgrEngineExample.jar
rem SET PROC_CLASSPATH=%PROC_CLASSPATH%":C:\Users\jose.alonsoc\Documents\Claro\LanzaProceso_v2.0\lib\AmdocsCore.jar"

@echo javac -Xlint:unchecked -classpath %PROC_CLASSPATH% 
"C:\Program Files\Java\jdk1.6.0_45\bin\"javac -verbose -Xlint:unchecked -classpath %PROC_CLASSPATH% LanzaProceso.java >salida.out
