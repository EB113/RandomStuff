#include <stdio.h>
#include <stdlib.h>
#include "validate.h"
#include "data.h"
#include "encoder.h"
#include "coms.h"

int main(int argc, char** argv) 
{   
    if(argc < MINARGSSIZE) 
    {
        fprintf(stderr,"NOT ENOUGH ARGUMENTS! TYPE -h/H FOR HELP!\n");
        return ERRINPUTSIZE;
    }

    SNMP_VB var_binds = (SNMP_VB)calloc(1,sizeof(struct snmp_varbind));

    if(inputValidate(argc, argv, var_binds) == 0)
    {
        SNMP session = snmpBuild(argc, argv, var_binds);
        unsigned char* pduRaw = snmpEncode(session);

        if(pduRaw){
            if(snmpTX(pduRaw)) return ERRTX;
        }
        
        pduPrint(session);
        snmpRelease(session, pduRaw);

    } else {
        VBRelease(var_binds);
        return ERRFLAG;
    }
    
    return 0;
}
