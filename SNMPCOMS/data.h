#ifndef DATA_H
#define DATA_H

#include <stdint.h>
#include <stddef.h>

//PRE-DEFINED DATATYPE SIZES
    //COMMUNITY STRING                   
    #define DEFCOMSTRIZE    6
    #define DEFCOMSTR       "public"


//SNMP DATATYPE
enum snmp_data_type {
    //SimpleSyntax
    SNMP_DATA_T_INTEGER                 = 0x02,
    SNMP_DATA_T_OCTET_STRING            = 0x04,
    SNMP_DATA_T_OBJECT                  = 0x06,
   
    //ApplicationSyntax
    SNMP_DATA_T_IPADDRESS               = 0x40,
    SNMP_DATA_T_COUNTER32               = 0x41,               
    SNMP_DATA_T_COUNTER64               = 0x46,
    SNMP_DATA_T_TIMETICKS               = 0x43,
    SNMP_DATA_T_OPAQUE                  = 0x44,
    SNMP_DATA_T_UNSIGNED32              = 0x42,

    SNMP_DATA_T_NULL                    = 0x05,
    SNMP_DATA_T_SEQUENCE                = 0x30,
};

//SNMP PRIMITIVE
enum snmp_prim_type {
    SNMP_DATA_T_PDU_GET_REQUEST         = 0xA0,
    SNMP_DATA_T_PDU_GET_NEXT_REQUEST    = 0xA1,
    SNMP_DATA_T_PDU_GET_RESPONSE        = 0xA2,
    SNMP_DATA_T_PDU_SET_REQUEST         = 0xA3,
    SNMP_DATA_T_PDU_TRAP                = 0xA4,
    SNMP_DATA_T_PDU_BULK                = 0xA5,
};

//SNMP ERROR
enum snmp_err_type {
    SNMP_NOERROR    = 0x00,
    SNMP_LARGE      = 0x01,
    SNMP_NOTFOUND   = 0x02,
    SNMP_NOMATCH    = 0x03,
    SNMP_READONLY   = 0x04,
    SNMP_GENERAL    = 0x05,
};

//SNMP VERSION
enum snmp_ver {
    SNMP_V1     = 0x00,
    SNMP_V2C    = 0x01,
    SNMP_V3     = 0x02,
};

//MUDAR PARA APENAS LONG LONG??
struct snmp_varbind {
    //OBJECT IDENTIFIER
    int         oidlen;
    enum        snmp_data_type oid_type;
    char**      oid;
    //VALUE
    int         vallen;
    enum        snmp_data_type val_type;
    union       snmp_varbind_val {
        int i;
        unsigned long long l;
        char **s;
    } value;

    struct snmp_varbind *next;
};
typedef struct snmp_varbind *SNMP_VB;

struct snmp {
    //EXTRA
    char*  output;

    //BASICS
    char*   ipAddress;
    int     port;

    /**PDU ELEMS**/
    enum snmp_ver       version;
    char*               comString;
    enum snmp_prim_type     primitive;
    int                         reqid;
    enum snmp_err_type          errstat;
    int                         errindex;

    SNMP_VB                         var_binds;
};
typedef struct snmp *SNMP;

SNMP snmpBuild(int argc, char** argv, SNMP_VB var_binds);
void snmpRelease(SNMP session, unsigned char* pduRaw);
void VBRelease(SNMP_VB var_binds);
void pduPrint(SNMP session);

//ASN1C
unsigned char* asn1c_coder();

#include "validate.h"

#endif