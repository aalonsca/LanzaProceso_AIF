#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#define RUTA_OUT "/home/amdocs/apmadm/JEE/CRM/procesos/out/"
#define RUTA_LOG "/home/amdocs/apmadm/JEE/CRM/procesos/log/"
#define APP "/home/amdocs/apmadm/JEE/CRM/procesos/LanzaProceso.ksh"
#define LEN_LINE 32
#define LEN_EXEC 500
#define LEN_FILE_NAME 500

char *main (int argc, char *argv[])
{
  FILE *f;

  char *_exec = malloc(LEN_EXEC);
  char *_exit = malloc(LEN_LINE); //como maximo sera un string de 31
  char *_objid = malloc(LEN_LINE);
  char *_fileName = malloc(LEN_FILE_NAME); //nombre del fichero a 30 

  if (argc < 3) {
	  strcpy(_fileName, RUTA_LOG);
      strcat(_fileName, "err.log");
	  f = fopen(_fileName, "a");
	  fprintf(f, "%s", "ERROR Numero argumentos invalido");
	  fclose(f);    
	  
	  strcpy(_exit, "ERROR");
      return _exit;

  }else{
  
	// generamos el nombre del fichero
	  strcpy(_fileName, RUTA_OUT);
	  strcat(_fileName, argv[1]);
	  strcat(_fileName, "_");
	  strcat(_fileName, argv[3]);
	  
	  //generamos la sentencia de ejecucion
	  strcpy(_exec, APP);
	  strcat(_exec, " '");
	  strcat(_exec, argv[1]);
	  strcat(_exec, "' ");
	  strcat(_exec, " '");
	  strcat(_exec, argv[2]);
	  strcat(_exec, "' ");
	  strcat(_exec, " '");
	  strcat(_exec, argv[3]);
	  strcat(_exec, "' ");
	  
	  // ejecutamos el comando
	  system (_exec);
	  
	  
	  //recuperamos el objid generado del fichero
	  f = fopen(_fileName, "r");
	  
	  fgets(_objid, LEN_LINE, f);
	  fclose(f);
	  
	  //printf("%s\n", _objid);
	  
	  if (strlen(_objid) > 0) {
		  strcpy(_exit, _objid);
		  strcpy(_exec, "rm -f ");
		  strcat(_exec, _fileName);
		  
		  //Si hemos recuperado la info, borramos el fichero
		  system(_exec);
	  }
	 
	  //Escribimos resultado
	  return _exit;
  }
}
