#ifndef VAL_H
#define VAL_H

#include "data.h"
#include "coms.h"
//REQUIREMENTS
#define MINARGSSIZE     2

//ERRORS
#define ERRINPUTSIZE    1
#define ERRTX           2
#define ERRINPUTVAL     3
#define ERRFLAG         4

//MISCELANEOUS
#define HELPLIST        "ENCGRv0.0\nENCODER FOR SNMP COMMANDS\n" \
                        "BUILD: [PRIM] [IPADDRESS] {[OID]} {[FLAG OPTIONS]}\n" \
                        "EXAMPLE: get 127.0.0.1 1.2 1.2.3.4 -p 230 -c public\n\n" \
                        "[PRIM]: VALID SNMP PRIMITIVE\n\n" \
                        "[IPADDRESS]: VALID IPV4 ADDRESS\n\n" \
                        "{[OID]}: INMF VALID OID LIST\n\n" \
                        "{[FLAG OPTIONS]}:\n" \
                        "\t-h/H : HELP OPTIONS\n" \
                        "\t-o/O[OPTION] :\n" \
                        "\t\t[OPTION]<-f/F : OUTPUT FILE NAME\n" \
                        "\t\t[OPTION]<-p/P : OUTPUT NAMED PIPE NAME\n" \
                        "\t\t[OPTION]<-s/S : OUTPUT SOCKET PORT\n" \
                        "\t-p/P : PORT OPTIONS\n" \
                        "\t-c/C : COMMUNITY STRING OPTIONS\n\n" \
                        "WARNING! PRE DEFINED VALUES ARE USED FOR:\n" \
                        "COMMUNITY STRING: PUBLIC\n" \
                        "PORT: 161\n" \
                        "VERSION: 1\n" \
                        "OUTPUT: STDOUT\n" \
                        "WARNING!"  \
                        "MAX VALUES:\n" \
                        "\tOID ELEM: 16383\n" \
                        "WARNING! SET PRIMITIVE:\n" \
                        "[OID] [TYPE] [VALUE]\n" \
                        "\t[TYPE]: \n" \
                        "\t\ti INTEGER \n" \
                        "\t\tu UNSIGNED \n" \
                        "\t\ts STRING \n" \
                        "\t\tx HEX STRING \n" \
                        "\t\td DECIMAL STRING \n" \
                        "\t\tn NULLOBJ \n" \
                        "\t\to OBJID \n" \
                        "\t\tt TIMETICKS \n" \
                        "\t\ta IPADDRESS \n" \
                        "\t\tb BITS \n" \
                        "WORKING FEATURES: \n" \
                        "\t-SNMPv1 \n" \
                        "\t-SNMP GET \n" \
                        "\t-SNMP SET \n" \
                        "\t-INTEGER AND STRING  VALUES on SNMPSET\n"
                        //1+1x7++0+1x7

int inputValidate(int argc, char** argv, SNMP_VB var_binds);

#endif