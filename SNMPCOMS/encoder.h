#ifndef ENCODER_H
#define ENCODER_H

#include "data.h"
#include <string.h>

struct oid_raw {
	unsigned char byteArray[2];
	int len;
	struct oid_raw *next;
};
typedef struct oid_raw *OID;

struct val_raw {
	unsigned char* byteArray;
	int len;
	enum        snmp_data_type type;
	struct val_raw *next;
};
typedef struct val_raw *VAL;

struct vb_raw {
	int oidhex_len;
	OID oid;
	int valhex_len;
	VAL val;
	struct vb_raw *next;
};
typedef struct vb_raw *VB;

unsigned char* snmpEncode(SNMP session);

#endif