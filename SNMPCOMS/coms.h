#ifndef COMS_H
#define COMS_H

#include "fcntl.h"
#include "unistd.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

enum out_opt {
    STDOUT		= 0x00,
    FILE_NAME 	= 0x01,
    NAMED_PIPE 	= 0x02,
    SOCKET 		= 0x03
};

extern enum out_opt opt;
extern char* opt_value;

int snmpTX(unsigned char* pduRaw);

#endif