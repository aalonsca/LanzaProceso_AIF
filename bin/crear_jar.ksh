#!/bin/ksh

#Generamos el .jar con las clases. ATENCION: hay que copiar el MANIFEST.MF al directorio en el que estan las clases y ejecutarlo desde alli
jar cvmf MANIFEST.MF LanzaProceso.jar $(find . -name "*.class")

#actualizamos con el resto de archivos necesarios
#mv LanzaProceso.jar ./../.
#jar uvf LanzaProceso.jar res lib
