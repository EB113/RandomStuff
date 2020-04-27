#include "encoder.h"
#include <stdlib.h>
#include <stdio.h>


void releaseOID(OID oid) 
{
	if(oid->next) releaseOID(oid->next);

	oid->next = NULL;
	free(oid);	

	return;
}

void releaseVAL(VAL val)
{
	if(val->next) releaseVAL(val->next);
	free(val->byteArray);
	val->next = NULL;
	free(val);	

	return;
}

void releaseBER(VB ber) 
{
	if(ber->next) releaseBER(ber->next);

		if(ber->oid)	releaseOID(ber->oid);
		if(ber->val)	releaseVAL(ber->val);

	ber->next = NULL;
	free(ber);	

	return;
}

//sizes still need to be fixed according to alterations in set validation function
//ONLY DONE NULL AND INTERGER < 0XFFFF
void berVBEncode_val(SNMP_VB var_binds, VB ber, int* tam)
{
	ber->val = (VAL)calloc(1,sizeof(struct val_raw));
	ber->val->type = var_binds->val_type;

	switch(ber->val->type){
		case SNMP_DATA_T_INTEGER:
			switch(var_binds->vallen){
				case 1://REDUNDACIA DE INFORMAÇÃO CODIGO NECESSITA DE OPTIMIZAÇÃO
					ber->val->len 		= var_binds->vallen;
					ber->valhex_len 	= var_binds->vallen+2;
					ber->val->byteArray = (unsigned char*)calloc(1,sizeof(unsigned char));
					ber->val->byteArray[0] = var_binds->value.i & 0xFF;
					(*tam)+=1+2;
				break;
				case 2:
					ber->val->len 		= var_binds->vallen;
					ber->valhex_len 	= var_binds->vallen+2;//TYPE+LENGTH
					ber->val->byteArray = (unsigned char*)calloc(1,sizeof(unsigned char));
					if((var_binds->value.i & 0xFF) < 0x80){
						ber->val->byteArray[1] = var_binds->value.i;
						ber->val->byteArray[0] = (((var_binds->value.i >> 8) & 0x3F)<<1)|0x80;
					}else{
						ber->val->byteArray[1] = var_binds->value.i & 0xEF;
						ber->val->byteArray[0] = (((var_binds->value.i >> 8) & 0x3F)<<1)|0x81;
					}
					(*tam)+=2+2;
				break;
				case 3:
					ber->val->len 		= var_binds->vallen;
					ber->valhex_len 	= var_binds->vallen;
					ber->val->byteArray = (unsigned char*)calloc(1,sizeof(unsigned char));
					ber->val->byteArray[0] = var_binds->value.i & 0xFF;
					(*tam)+=3+2;
				break;
				case 4:
					ber->val->len 		= var_binds->vallen;
					ber->valhex_len 	= var_binds->vallen;
					ber->val->byteArray = (unsigned char*)calloc(1,sizeof(unsigned char));
					ber->val->byteArray[0] = var_binds->value.i & 0xFF;
					(*tam)+=4+2;
				break;
				default:
					printf("DEFAULT INTEGER ??\n");
				break;
			}
		break;
		case SNMP_DATA_T_OCTET_STRING:
			printf("SNMP_DATA_T_OCTET_STRING");
		break;
		case SNMP_DATA_T_OBJECT:
			printf("SNMP_DATA_T_OBJECT");		
		break;
		case SNMP_DATA_T_IPADDRESS:
			printf("SNMP_DATA_T_IPADDRESS");
		break;
		case SNMP_DATA_T_COUNTER32:
			printf("SNMP_DATA_T_COUNTER32");
		break;
		case SNMP_DATA_T_COUNTER64:
			printf("SNMP_DATA_T_COUNTER64");
		break;
		case SNMP_DATA_T_TIMETICKS:
			printf("SNMP_DATA_T_TIMETICKS");
		break;
		case SNMP_DATA_T_OPAQUE:
			printf("SNMP_DATA_T_OPAQUE");
		break;
		case SNMP_DATA_T_UNSIGNED32:
			printf("SNMP_DATA_T_UNSIGNED32");
		break;
		case SNMP_DATA_T_NULL:
			ber->valhex_len = 2;
			ber->val->len  			= 1;
			ber->val->byteArray 	= (unsigned char*)malloc(sizeof(unsigned char));
			ber->val->byteArray[0] 	= 0x00;
			//TYPE NULL +SIZE0
			(*tam)+=2;
		break;
		default:
			printf("WASH?\n");
		break;
	}

	return;
}


VB berVBEncode_init(SNMP_VB var_binds, int *tam)
{	
	VB ber = (VB)calloc(1,sizeof(struct vb_raw));
	(*tam)+=2;	//TYPE+TAM

	if(var_binds->next) ber->next = berVBEncode_init(var_binds->next, tam);

	if(var_binds->oidlen > 1) {
		ber->oid = (OID)calloc(1,sizeof(struct oid_raw));
		(*tam)+=2;//SEQUENCEOID+SIZE

		char** tmp = NULL;
		if(var_binds) {
			tmp = var_binds->oid;

			//INITIALIZE OID (40*X)+Y
			int aux = ((40*atoi(tmp[0]))+atoi(tmp[1]));
			if(aux<=0xFF){
				ber->oid->byteArray[0] 	= aux & 0xFF;
				ber->oid->len			= 1;
				(*tam)++;
				ber->oidhex_len++;
			}else return NULL;
			//WALK +2 ON OID CHAIN
			tmp+=2;
		}else	return NULL;

		if(tmp){
			OID iter = ber->oid;

			while(*tmp) {
				iter->next = (OID)calloc(1,sizeof(struct oid_raw));
				iter = iter->next;
				int x = atoi(*tmp);

				if(x < 0x80){
					iter->byteArray[0] = x;
				    iter->len = 1;
				    (*tam)++;
				    ber->oidhex_len++;
				}else{
					if((x & 0xFF) < 0x80){
						iter->byteArray[1] = x;
						iter->byteArray[0] = (((x >> 8) & 0x3F)<<1)|0x80;
					}else{
						iter->byteArray[1] = x & 0xEF;
						iter->byteArray[0] = (((x >> 8) & 0x3F)<<1)|0x81;
					}
	 				iter->len = 2;
	 				(*tam)+=2;
	 				ber->oidhex_len+=2;
				}
				tmp++;
			}
		}

		//DEAL WITH oidlen_hex size = len+type+length HERE, but in bervbencode temporary place
		/*if(ber->oidhex_len < 0x80){
			printf("FUCK YEAH:%d\n\n",ber->oidhex_len);
			ber->oidhex_len+=2;
		}else{
			ber->oidhex_len+=3; //??WHATS MAX VALUE?INCOPLETE:MORE CASES NEED TO BE MADE
		}*/
	}else if(var_binds->oidlen == 1)	return NULL;//SMALL AMOUNT OF OID LEVEL 2 

	berVBEncode_val(var_binds, ber, tam);

	return ber;
}

unsigned char* berVBEncode(VB ber, int* tam)
{
    //BUILD UNSIGNED CHAR* BER RAW DATA
	if(ber){
	    int counter = 0;
		(*tam)		+=2;//VARBINDSEQUENCE+SIZE
	    unsigned char* pduRaw = (unsigned char*)calloc((*tam)*2+1, sizeof(unsigned char));

		//VARBINDSEQUENCE+SIZE
		sprintf((char*)(&pduRaw[counter*2]),"%02x",SNMP_DATA_T_SEQUENCE);
		counter++;
		sprintf((char*)(&pduRaw[counter*2]),"%02x",(*tam)-2);
		counter++;

	    VB iter 	= ber;
	    OID oidIter = NULL;
	    VAL valIter = NULL;
	    while(iter) {

	    	oidIter = iter->oid;
	    	if(oidIter){
	    		//SEQUENCETYPE+TAM
	    		sprintf((char*)(&pduRaw[counter*2]),"%02x",SNMP_DATA_T_SEQUENCE);
	    		counter++;

//-------------------------
//CASES SHOULD BE MADE FOR VALUES ABOVE 128
	    		int aux_type_val_len = 2;
    			sprintf((char*)(&pduRaw[counter*2]),"%02x",iter->oidhex_len + aux_type_val_len + iter->valhex_len);
	    		counter++;


	    		//OIDTYPE+TAM
	    		sprintf((char*)(&pduRaw[counter*2]),"%02x",SNMP_DATA_T_OBJECT);
	    		counter++;
	    		sprintf((char*)(&pduRaw[counter*2]),"%02x",iter->oidhex_len);
	    		counter++;
//-------------------------
		    	while(oidIter){
			    	//OBJECT IDENTIFIER
					if(oidIter && oidIter->len == 1){
						sprintf((char*)(&pduRaw[counter*2]),"%02x",oidIter->byteArray[0]);
						counter++;
					}
			    	else if(oidIter && oidIter->len == 2){
			    		sprintf((char*)(&pduRaw[counter*2]),"%02x",oidIter->byteArray[0]);
			    		counter++;
			    		sprintf((char*)(&pduRaw[counter*2]),"%02x",oidIter->byteArray[1]);
			    		counter++;
			    	}	
			    	oidIter = oidIter->next;
		    	}
	    	}
	    	//NULL DOESNT HAVE VALUE ONLY TYPE AND LENGTH
	    	valIter = iter->val;
    		if(valIter){
	    		while(valIter){
			    	//NULL+SIZE0
			    	if(valIter->type == SNMP_DATA_T_NULL){
				    	sprintf((char*)(&pduRaw[counter*2]),"%02x",SNMP_DATA_T_NULL);
			    		counter++;
			    		sprintf((char*)(&pduRaw[counter*2]),"%02x",valIter->byteArray[0]);
			    		counter++;

	    			}else{
	    				sprintf((char*)(&pduRaw[counter*2]),"%02x",valIter->type);
	    				counter++;
	    				sprintf((char*)(&pduRaw[counter*2]),"%02x",valIter->len);
	    				counter++;
	    				for(int i = 0; i<valIter->len; i++)
	    					sprintf((char*)(&pduRaw[counter++*2]),"%02x",valIter->byteArray[i]);
	    			}

		    		valIter = valIter->next;
	    		}
    		}

	    	iter = iter->next;
	    }

	    //RELEASE STRUCTS
		releaseBER(ber);
	    return pduRaw;
	}

	return NULL;
}

int int_hex_size(int num)
{
	int size = 0;

	if(num < 128)
	{
		return 1;
	}else if(num < 16384){
		return 2;
	}else{
		return -1;
	}

	return size;
}


unsigned char* snmpv1Encode(SNMP session)
{	
	int tam 					= 0; //SIZE OF VARBIND LIST
	VB ber_init  				= NULL;
	unsigned char* com_ber 		= NULL;
	unsigned char* header_ber 	= NULL;
	unsigned char* vb_ber 		= NULL;
	unsigned char* snmp_ber		= NULL;

	//VarBinds Encoding BER
	if(session && session->var_binds){
		ber_init 	= berVBEncode_init(session->var_binds, &tam);
		vb_ber 		= berVBEncode(ber_init, &tam);
	}

	if(session){
		int msg_tam			= 2;//TYPE+LENGTH
		int ver_tam			= 3;//TYPE+LENGTH+VALUE
		int comstring_tam	= 2 + strlen(session->comString);//TYPE+LENGTH+VALUE
		int snmp_tam		= 2;//TYPE+LENGTH
		int id_tam 			= 2 + int_hex_size(session->reqid);//TYPE+LENGTH+VALUE //STILL NEED TO BE DONE
		int err_tam 		= 3;//TYPE+LENGTH+VALUE
		int index_tam		= 2 + int_hex_size(session->errindex);//TYPE+LENGTH+VALUE //STILL NEED TO BE DONE

		//SNMP PDU Size to Hexa
		//Still needs to be done
		//Community String Encoding BER
		int com_len = strlen(session->comString);
		com_ber		= (unsigned char*)calloc(com_len*2+1,sizeof(unsigned char));
		for(int i = 0; i<com_len; i++)
			sprintf((char*)&(com_ber[i*2]),"%02x", session->comString[i]);

		//Header Encoding BER//com_len restricted?//+1 no header_tam incognito
		int header_tam 	= msg_tam+ver_tam+comstring_tam+snmp_tam+id_tam+err_tam+index_tam+1;//because %s in snprintf to correct make loop for com string
		header_ber		= (unsigned char*)calloc(header_tam*2+1,sizeof(unsigned char));
		snprintf((char*)header_ber, header_tam*2, 
			"%02x%02x%02x%02x%02x%02x%02x%s%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x", 
			0x30, header_tam + tam - 2 -1, SNMP_DATA_T_INTEGER, 0x01, session->version, SNMP_DATA_T_OCTET_STRING, 
			com_len, com_ber, session->primitive, tam+err_tam+id_tam+index_tam, SNMP_DATA_T_INTEGER, 0x01, session->reqid, 
			SNMP_DATA_T_INTEGER, 0x01, session->errstat, SNMP_DATA_T_INTEGER, 0x01, session->errindex);

		//Final PDU Encoding BER
		int final_tam 	= header_tam + tam;
		snmp_ber 		= (unsigned char*)calloc(final_tam*2+1,sizeof(unsigned char));
		snprintf((char*)snmp_ber, final_tam*2, "%s%s",header_ber,vb_ber);

		//Release Temp Allocs
		free(com_ber);
		free(vb_ber);
		free(header_ber);

		return snmp_ber;
	}

	return NULL;
}

unsigned char* snmpv2cEncode(SNMP session)
{
	return NULL;
}

unsigned char* snmpv3Encode(SNMP session)
{
	return NULL;
}


//FUNCTION FOR PDU ENCODING
unsigned char* snmpEncode(SNMP session)
{
	
	//Main Structure Encoding
	if(session){
		switch(session->version){
			case SNMP_V1:
				return snmpv1Encode(session);
			break;
			case SNMP_V2C:
				return snmpv2cEncode(session);
			break;
			case SNMP_V3:
				return snmpv3Encode(session);
			break;
			default:
				return NULL;
			break;
		}
	}

	return NULL;
}