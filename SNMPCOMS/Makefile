CC=gcc
CFLAGS=-g -Wall
OBJ1=snmpcoder.c
OBJ2=coms.c
OBJ3=encoder.c
OBJ4=data.c
OBJ5=validate.c

ENCODER: $(OBJ1)
	$(CC) $(CFLAGS) $(OBJ1) $(OBJ2) $(OBJ3) $(OBJ4) $(OBJ5) -o encoder

.PHONY: clean

clean: rm encoder
