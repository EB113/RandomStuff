#include "coms.h"
#include "stdio.h"
#include "stdlib.h"
#include "string.h"

enum out_opt opt;
char* opt_value;


int snmpTX_out(unsigned char* pduRaw)
{
	printf("\nPDU:%s\n",pduRaw);
	printf("OUTPUT: STDOUT\n");
	
	return 0;
}
int snmpTX_file(unsigned char* pduRaw)
{
	int fd = open(opt_value,O_WRONLY, 0644);
    if(fd<0){
        return 1;
    }

    int error = 0;
    while(*pduRaw != '\0' && error >=0) error = write(fd,pduRaw++,1);

    free(opt_value);
    close(fd);
    if(error<0) return -1;

	return 0;
}
int snmpTX_fifo(unsigned char* pduRaw)
{
	int fd = open(opt_value,O_WRONLY, 0644);
    if(fd<0){
        return 1;
    }

    int error = 0;
    while(*pduRaw != '\0' && error >=0) error = write(fd,pduRaw++,1);

    free(opt_value);
    close(fd);
    if(error<0) return -1;
	return 0;
}
int snmpTX_sock(unsigned char* pduRaw)
{
	//CODE FROM:http://matrixsust.blogspot.pt/2011/10/udp-server-client-in-c.html
	int sock;
	struct sockaddr_in server_addr;
	struct hostent *host;

	host= (struct hostent *) gethostbyname((char *)"127.0.0.1");


	if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) == -1)
	{
	return -1;
	}

	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(atoi(opt_value));
	server_addr.sin_addr = *((struct in_addr *)host->h_addr);
	bzero(&(server_addr.sin_zero),8);


	sendto(sock, pduRaw, strlen(((char*)pduRaw)), 0,
              (struct sockaddr *)&server_addr, sizeof(struct sockaddr));


	free(opt_value);

	return 0;
}



//FUNCTION FOR PDU TRANSMISSION
int snmpTX(unsigned char* pduRaw)
{
	switch(opt){
		case STDOUT:
			return snmpTX_out(pduRaw);
		break;
		case FILE_NAME:
			return snmpTX_file(pduRaw);
		break;
		case NAMED_PIPE:
			return snmpTX_fifo(pduRaw);
		break;
		case SOCKET:
			return snmpTX_sock(pduRaw);
		break;
		default:
			return 1;
		break;

	}
    return 0;
}
