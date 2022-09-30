# Project: LanzaProceso_C
# Makefile created by Dev-C++ 5.11

#  ------------ Macros de Usuario ------------
#Directorio de trabajo
DIR_WORK=$(HOME)/LanzaProceso/

#Directorio de includes de usuario
INCLUDE = -Iinclude

#####

# Fichero fuente, objeto y libreria
FTE = LanzaProceso.c
OBJ = LanzaProceso.o
LIB = LanzaProceso.so
EXE = LanzaProceso

# Makefile
MAK = Makefile.mak

# Compilador
CC = gcc
LD = gcc

# Flags de compilacion
CCFLAGS = -m64 -fPIC

# Flags de linkado
LDFLAGS = -m64 -fPIC

# Dependencias
# El linkado dependera de la compilacion de los ficheros objeto
$(LIB): $(OBJ)
	@echo "link >>"
	$(LD) $(LDFLAGS) $(OBJ) -o $(LIB)
	@echo "guardando libreria >>"

# La compilacion dependera de la modificacion del fuente o del Makefile
$(OBJ): $(FTE) $(MAK)
	@echo "compila >>"
	$(CC) $(CCFLAGS) $(INCLUDE) -c $(FTE) -o $(OBJ)

$(EXE): $(FTE) $(MAK)
	@echo "ejecutable >>"
	$(CC) $(CCFLAGS) $(INCLUDE) $(OBJ) -o $(EXE)
