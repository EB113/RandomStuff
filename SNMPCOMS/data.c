#include "data.h"
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <stdio.h>


void pduPrint(SNMP session){
    int counter = 0;
    printf("####SNMP DATA####\n\n");
    printf("ipAddress: %s\n",session->ipAddress);
    printf("port: %d\n",session->port);
    switch(session->version){
        case SNMP_V1:
            printf("version: 1\n");
        break;
        case SNMP_V2C:
            printf("version: 2c\n");
        break;
        case SNMP_V3:
            printf("version: 3\n");
        break;
        default:
            printf("version: ??\n");
        break;
    }
    printf("comString: %s\n",session->comString);
    switch(session->primitive){
        case SNMP_DATA_T_PDU_GET_REQUEST:
            printf("primitive: GET\n");
        break;
        case SNMP_DATA_T_PDU_GET_NEXT_REQUEST:
            printf("primitive: GET_NEXT\n");
        break;
        case SNMP_DATA_T_PDU_GET_RESPONSE:
            printf("primitive: GET_RESPONSE\n");
        break;
        case SNMP_DATA_T_PDU_SET_REQUEST:
            printf("primitive: SET_REQUEST\n");
        break;
        case SNMP_DATA_T_PDU_TRAP:
            printf("primitive: TRAP\n");
        break;
        case SNMP_DATA_T_PDU_BULK:
            printf("primitive: BULK\n");
        break;
        default:
            printf("primitive: ??\n");
        break;
    }
    SNMP_VB VB_iter = session->var_binds;
    while(VB_iter){
        printf("----------VarBind %d---------\n", counter++);
        printf("OID len: %d\n", VB_iter->oidlen+1);
        printf("OID: ");
        char** oid_iter = VB_iter->oid;
        while(*oid_iter) {
            printf("%s", *oid_iter);
            if(*(++oid_iter))printf(".");
        }
        printf("\n");
        char** val_iter = NULL;
        printf("VAL len: %d\n",VB_iter->vallen);
        switch(VB_iter->val_type){
            case 0x02:
                printf("TYPE: INTEGER\n");
                printf("VAL: %d\n",VB_iter->value.i);               
            break;
            case 0x04:
                printf("TYPE: OCTET_STRING\n");
                printf("VAL: ");
                val_iter = VB_iter->value.s;
                while(*val_iter) {
                    printf("%s", *val_iter);
                    if(*(++val_iter))printf(".");
                }
                printf("\n");
            break;
            case 0x06:
                //POSSIBLE??
                printf("TYPE: OBJECT\n");
                printf("VAL: ");
                val_iter = VB_iter->value.s;
                while(*val_iter) {
                    printf("%s", *val_iter);
                    if(*(++val_iter))printf(".");
                }
                printf("\n");
            break;
            case 0x40:
                printf("TYPE: IPADDRESS\n");
                printf("VAL: ");
                val_iter = VB_iter->value.s;
                while(*val_iter) {
                    printf("%s", *val_iter);
                    if(*(++val_iter))printf(".");
                }
                printf("\n");
            break;
            case 0x41:
                printf("TYPE: COUNTER32\n");
                printf("VAL: %lu\n",(unsigned long)VB_iter->value.l);
            break;
            case 0x46:
                printf("TYPE: COUNTER64\n");
                printf("VAL: %llu\n",VB_iter->value.l);
            break;
            case 0x43:
                printf("TYPE: TIMETICKS\n");
                printf("VAL: %lu\n",(unsigned long)VB_iter->value.l);
            break;
            case 0x44:
                //MAS QUE E ISTO??
                printf("TYPE: OPAQUE\n");
                printf("VAL: ");
                val_iter = VB_iter->value.s;
                while(*val_iter) {
                    printf("%s", *val_iter);
                    if(*(++val_iter))printf(".");
                }
                printf("\n");
            break;
            case 0x42:
                printf("TYPE: UNSIGNED32\n");
                printf("VAL: %lu\n",(unsigned long)VB_iter->value.l);
            break;
            case 0x05:
                printf("TYPE: NULL\n");
                printf("VAL: NULL\n");
            break;
            default:
                printf("TYPE: ??\n");
                printf("VAL: ??\n");
            break;
        }
        VB_iter = VB_iter->next;
    }
}

void VBRelease(SNMP_VB var_binds) 
{
    while(var_binds) {
        if(var_binds->oid){
            for(int i=0; i<var_binds->oidlen+1; i++) free(var_binds->oid[i]);
            free(var_binds->oid);
        }
        SNMP_VB tmp        = var_binds;
        var_binds = var_binds->next;
        free(tmp);
    }
}


//FUNCTION FOR HEAP RELEASE
void snmpRelease(SNMP session, unsigned char* pduRaw)
{   
    free(session->ipAddress);
    free(session->comString);
    free(session->output);
    VBRelease(session->var_binds);
    free(session);
    free(pduRaw);

    return;
}
//FUNCTION FOR PRIMITIVE TO HEX
unsigned char getPrimitive(char* prim)
{
    switch(prim[0]){
        case 'g':
            switch(strlen(prim)){
                case 3:
                    return SNMP_DATA_T_PDU_GET_REQUEST;
                break;
                case 7:
                    return SNMP_DATA_T_PDU_GET_NEXT_REQUEST;
                break;
                default:
                    return '\0';
                break;
            } 
        break;
        case 's':
            return SNMP_DATA_T_PDU_SET_REQUEST;
        break;
        case 't':
            return SNMP_DATA_T_PDU_TRAP;
        break;
        case 'b':
            return SNMP_DATA_T_PDU_BULK;
        break;
        default:
            return '\0'; 
        break;
    }
}


//FUNCTION FOR SNMP STRUCT BUILDER
SNMP snmpBuild(int argc, char** argv, SNMP_VB var_binds)
{
    int counter = 0;

    //STRUCTURE BUILD
    SNMP session            = (SNMP)calloc(1,sizeof(struct snmp));
    session->var_binds      = var_binds;
    SNMP_VB vb_init         = var_binds;

    //PRE DEFINED VALUES
    session->port       = 161;
    //session->tag      = 
    session->comString  = strdup(DEFCOMSTR);
    session->version    = SNMP_V1;

    for(int i=1; i< argc; i++)
    {
        if(argv[i][0] == '-'){
            switch(tolower(argv[i++][1]))
            {
                case 'o':
                    session->output = strdup(argv[i]);
                    break;
                case 'v':
                    switch(argv[i][0]){
                        case '1':
                            session->version = SNMP_V1;
                        break;
                        case '2':
                            session->version = SNMP_V2C;
                        break;
                        case '3':
                            session->version = SNMP_V3;
                        break;
                        default:
                            session->version = SNMP_V1;
                        break;
                    }

                    break;
                case 'p':
                    session->port = atoi(argv[i]);
                    break;
                case 'c':
                    free(session->comString);
                    session->comString = strdup(argv[i]);
                    break;
                default:
                    break;
            }
        }else {
            switch(counter)
            {
                case 0:
                    session->primitive = getPrimitive(argv[i]);
                    counter++;
                    break;
                case 1:
                    session->ipAddress = (char*)calloc(strlen(argv[i])+1, sizeof(char));
                    strcpy(session->ipAddress, argv[i]);
                    counter++;
                    break;
                default: ;//empty statement work this
                    //oidlen=# of '.', +2 one more number and last one NULL for iteration
                    //OBJECT IDETIFIER FIELD
                    session->var_binds->oid = (char**)calloc(session->var_binds->oidlen+2, sizeof(char*));
                    char* tmp = strdup(argv[i]);
                    int j = 0;
                    //OID TREAT
                        char *token;
                        const char s[2] = ".";
                        /* get the first token */
                        token = strtok(tmp, s);
                        /* walk through other tokens */
                        while( token != NULL ) {
                            session->var_binds->oid[j++] = strdup(token);
                            token = strtok(NULL, s);
                        }
                        session->var_binds->oid_type = SNMP_DATA_T_OBJECT;
                    //VALUE TREAT
                        if(session->primitive == SNMP_DATA_T_PDU_SET_REQUEST){
                            //ESTRUTURA FOI INCIALMENTE MAL DESENHADA VALIDAÇÃO E 
                            //CRIAÇÃO DE ESTRUTURA DEVERIA SER FEITO Na MESMa FUNCAO
                            //NA VALIDACAO JA SE INSERIU OS VALORES AQUI APENAS SALTAMOS
                            //
                            //DEAL TYPE
                            i++;
                            //DEAL VALUE
                            i++;
                        }else{
                            //VALUE FIELD
                            session->var_binds->vallen = 0;
                            session->var_binds->val_type = SNMP_DATA_T_NULL;
                        }

                    session->var_binds = session->var_binds->next;
                    counter++;
                    free(tmp);
                    break;
            }
        }
    }

    session->var_binds = vb_init;
    return session;
}
