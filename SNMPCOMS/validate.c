#include "validate.h"
#include <string.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>

unsigned char primitive = 0x00;
unsigned char version;
enum out_opt opt = STDOUT;
char* opt_value  = NULL;

//ARE THESE THE VALUES?
int verValidate(char* gem)
{
    if(strcmp(gem, "1") == 0) {version = 0x00; return 0;}
    if(strcmp(gem, "2c") == 0) {version = 0x01; return 0;}
    if(strcmp(gem, "3") == 0) {version = 0x02; return 0;}

    return 1;
}

int outValidate(char* gem, char* option)
{
    int len = strlen(gem);
    int fd = 0;
    char bad_chars[] = "!@%^*~|";
    int invalid_found = 0;

    if(len < 20)
    {
        //TRY TO OPEN FILE, if ok assign variable fp from coms.c
        switch(tolower(option[2])){
            case 'f':
                //code from https://stackoverflow.com/questions/10159230/validating-user-input-for-filenames
                for (int i = 0; i < strlen(bad_chars); ++i) {
                    if (strchr(gem, bad_chars[i]) != NULL) {
                        invalid_found = 1;
                        break;
                    }
                }
                if (invalid_found) {
                    return 1;
                }

                fd = open(gem,O_WRONLY | O_CREAT, 0644);
                if(fd<0){
                    return 1;
                }else close(fd);

                opt = FILE_NAME;
                opt_value = strdup(gem);
            break;
            case 'p':
                //code from https://stackoverflow.com/questions/10159230/validating-user-input-for-filenames
                for (int i = 0; i < strlen(bad_chars); ++i) {
                    if (strchr(gem, bad_chars[i]) != NULL) {
                        invalid_found = 1;
                        break;
                    }
                }
                if (invalid_found) {
                    return 1;
                }
                
                mkfifo(gem, 0644);/*
                fd = 0;
                fd = open(gem,O_WRONLY | O_NONBLOCK, 0644);
                if(fd<0){
                    return 1;
                }else close(fd);*/

                opt = NAMED_PIPE;
                opt_value = strdup(gem);
            break;
            case 's':
                for(int i=0; i<len; i++)
                {
                    if(!isdigit(gem[i])) return 1;
                } 
                int sock = 0;
                if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) == -1)
                {
                    return 1;
                }

                opt = SOCKET;
                opt_value = strdup(gem);
            break;
            default:
                return 1;
            break;
        }

        return 0;
    }


    return 1;
}

int portValidate(char* gem)
{
    int len = strlen(gem);

    if(len < 6)
    {
        for(int i=0; i<len; i++)
        {
            if(!isdigit(gem[i])) return 1;
        }
        int tmp = atoi(gem);
        if(tmp>0 && tmp< 65535) return 0;
    }

    return 1;
}

//STILL NEED TO BE WORKED
int comValidate(char* gem)
{
    int len = strlen(gem);
    if(len < 127)
    {
        return 0; 
    } 
    return 1;
}

int primValidate(char* gem)
{
    if(strcmp(gem, "set") == 0)         {primitive = 0xA3; return 0;}
    if(strcmp(gem, "get") == 0)         return 0;
    if(strcmp(gem, "getnext") == 0)     return 0;
    if(strcmp(gem, "walk") == 0)        return 0;
    if(strcmp(gem, "bulkget") == 0)     return 0;
    if(strcmp(gem, "bulkwalk") == 0)    return 0;
    if(strcmp(gem, "trap") == 0)        return 0;

    return 1;
}
int adressValidate(char* gem)
{
    int i, bool = 0, counter = 0;
    for(i=0; i<strlen(gem); i++)
    {
        if(gem[i] == '.' && bool)
        {
            return 1;
        }
        else if(!isdigit(gem[i]) && gem[i] != '.')
        {
            return 1;
        }
        else if(gem[i] == '.') {
            counter++;
            bool=1;
        }
        else bool = 0;
    }
    if(counter != 3) {printf("%d\n",counter); return 1;}

    return 0;   
}
//NEED TO VALIDATE VALUE < 16383
int oidValidate(char* gem, SNMP_VB var_binds)
{
    int bool = 0;

    for(int i=0; i<strlen(gem); i++)
    {
        if((!isdigit(gem[i]) && gem[i] != '.') || (gem[i] == '.' && bool))
        {
            return 1;
        }
        if(gem[i] == '.')
        {
            var_binds->oidlen++;
            bool = 1;
        }
        else bool = 0;
    }
    return 0;
}


//FUNCTIONS MUST RETURN -1 FOR ERROR OR VAL LENGTH
//int max 2147483647 int min –2147483647 – 1
//THIS IS WRONG!!!RETURN LENGTH NEEDS TO BE FIXED
int num_validate(char* gem, int byte_size, int type)
{
    int i = 0, size = 0;
    void* aux = NULL;

//NEEDS TO BE CHANGED OR UNSIGNED CAN BE REPRESENTED WITH '-'
    while(gem[i] != '\0'){
        if(gem[i] == '-') i++;
        if(!isdigit(gem[i++])) return 0;
    }
    if(i>10) return 0;
    //Validation is wrong number cant be FF highest value bit in 1 means next byte is number
    //INTEGERS

    if(type == 0){
        aux = (int*)calloc(1,sizeof(int));
        *((int*)aux) = atoi(gem);
        if(*((int*)aux)<=0xFF){
            size = 1;
        }else if(*((int*)aux)<=0xFFFF){
            size = 2;
        }else if(*((int*)aux)<=0xFFFFFF){
            size = 3;
        }else if(*((int*)aux)<=0xFFFFFFFF){
            size = 4;
        }else size = 0;
    }else if(type == 1){//UNSIGNED INT
        aux = (unsigned int*)calloc(1,sizeof(unsigned int));
        *((unsigned int*)aux) = strtoul(gem, NULL, 10);
        if(*((unsigned int*)aux)<=0xFF){
            size = 1;
        }else if(*((unsigned int*)aux)<=0xFFFF){
            size = 2;
        }else if(*((unsigned int*)aux)<=0xFFFFFF){
            size = 3;
        }else if(*((unsigned int*)aux)<=0xFFFFFFFF){
            size = 4;
        }else size = 0;
    }else if(type == 2){//UNSIGNED LONG LONG
        aux = (unsigned long long*)calloc(1,sizeof(unsigned long long));
        *((unsigned long long*)aux) = strtoull(gem, NULL, 10);
        if(*((unsigned long long*)aux)<=0xFF){
            size = 1;
        }else if(*((unsigned long long*)aux)<=0xFFFF){
            size = 2;
        }else if(*((unsigned long long*)aux)<=0xFFFFFF){
            size = 3;
        }else if(*((unsigned long long*)aux)<=0xFFFFFFFF){
            size = 4;
        }else if(*((unsigned long long*)aux)<=0xFFFFFFFFFF && byte_size > 4){
            size = 5;
        }else if(*((unsigned long long*)aux)<=0xFFFFFFFFFFFF && byte_size > 4){
            size = 6;
        }else if(*((unsigned long long*)aux)<=0xFFFFFFFFFFFFFF && byte_size > 4){
            size = 7;
        }else if(*((unsigned long long*)aux)<=0xFFFFFFFFFFFFFFFF && byte_size > 4){
            size = 8;
        }else size = 0;
    }

    free(aux);

    return size;
}

int str_validate(char* gem)
{
    return 1;
}

//SNMP SET INPUT VALIDATION
int setValidate(char* gemtype, char* gemvalue, SNMP_VB var_binds)
{
    int error = 0;

    switch(gemtype[0]){
        case 'i':
            error = num_validate(gemvalue,4,0);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        case 's':
            error = str_validate(gemvalue);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        case 'u':
           error = num_validate(gemvalue,4,1);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        break;
        case 'x':
            error = str_validate(gemvalue);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        case 'd':
            error = str_validate(gemvalue);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        case 'n':
            error = str_validate(gemvalue);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        case 'o':
            error = str_validate(gemvalue);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        case 't':
            error = num_validate(gemvalue,4,1);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        case 'a':
            error = str_validate(gemvalue);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        case 'b':
            error = str_validate(gemvalue);
            if(error>0) {
                var_binds->val_type     = SNMP_DATA_T_INTEGER;
                var_binds->value.i      = atoi(gemvalue);
                var_binds->vallen       = error;
                return 0;
            }
        break;
        default:
            return 1;
        break;
    }

    return error;
}

//FUNCTION FOR INPUT VALIDATION
int inputValidate(int argc, char** argv, SNMP_VB var_binds)
{
    int counter = 0, error = 0;
    SNMP_VB var_binds_iter = var_binds;

    for(int i=1; i< argc; i++)
    {
        if(argv[i][0] == '-'){
            switch(tolower(argv[i++][1]))
            {
                case 'h':
                    fprintf(stdout,HELPLIST);
                    return -1;
                    break;
                case 'v':
                    error = verValidate(argv[i]);
                    if(error){
                        fprintf(stderr,"WRONG VERSION:%s!\n",argv[i]);
                        return ERRINPUTVAL;
                    } 
                    break;
                case 'o':
                    error = outValidate(argv[i], argv[i-1]);
                    if(error){
                        fprintf(stderr,"WRONG OUT NAME:%s!/PIPE OR SOCKET CLOSED!\n",argv[i]);
                        return ERRINPUTVAL;
                    }
                    //ADD MORE ERRORS LIKE FOR FIFO NO READERS,ETC 
                    break;
                case 'p':
                    error = portValidate(argv[i]);
                    if(error){
                        fprintf(stderr,"WRONG PORT:%s!\n",argv[i]);
                        return ERRINPUTVAL;
                    } 
                    break;
                case 'c':
                    error = comValidate(argv[i]);
                    if(error){
                        fprintf(stderr,"WRONG COMMUNITY STRING:%s!\n",argv[i]);
                        return ERRINPUTVAL;
                    } 
                    break;
                default:
                    fprintf(stderr, "INVALID FLAG %c!\n", argv[i][1]);
                    return ERRFLAG;
                    break;
            }
        }else {
            switch(counter)
            {
                case 0:
                    error = primValidate(argv[i]);
                    if(error){
                        fprintf(stderr,"WRONG PRIMITIVE:%s!\n",argv[i]);
                        return ERRINPUTVAL;
                    } 
                    counter++;
                    break;
                case 1:
                    error = adressValidate(argv[i]);
                    if(error){
                        fprintf(stderr,"WRONG ADDRESS:%s!\n",argv[i]);
                        return ERRINPUTVAL;
                    } 
                    counter++;
                    break;
                default:
                    if(counter>2){
                        var_binds_iter->next    = (SNMP_VB)calloc(1,sizeof(struct snmp_varbind));
                        var_binds_iter          = var_binds_iter->next;
                    }
                    error = oidValidate(argv[i], var_binds_iter);
                    if(error){
                        fprintf(stderr,"WRONG OID:%s!\n",argv[i]);
                        return ERRINPUTVAL;
                    }
                    if(primitive == 0xA3){
                        i++;
                        //VALIDATE EXISTENCE OF TYPE AND VALUE and Validate values
                        //CASE FOR NULL n only
                        if(argv[i] && argv[i+1]) error = setValidate(argv[i], argv[i+1],var_binds_iter);
                        else error = 1;
                        i++;//SE em cima da warning
                        if(error){
                            fprintf(stderr,"WRONG SET VALUE:%s!\n",argv[i]);
                            return ERRINPUTVAL;
                        }
                    }
                    counter++;
                    break;
            }
        }

    }
    if(counter < 3) 
    {
        fprintf(stderr,"NOT ENOUGH ARGUMENTS! TYPE -h/H FOR HELP!\n");
        return ERRINPUTVAL;
    }

    return 0;
}