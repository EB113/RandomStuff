import sys,os
import readline
import pyfiglet

from .miscellaneous.completer import *
from .settings import *

############ OUTPUT GRAPHICS ################
class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

############### COMMAND LINE FUNCTIONS ##############
def example(cmd=None):
    if cmd != None and len(cmd) == 2:
        try:
            if(os.path.exists(cmd[1])):
                os.system(config.editor + " " + cmd[1])
            else:
                print("{}File does not exists!{}".format(bcolors.WARNING,bcolors.ENDC))
        except:
            print("{}Usage: example <FILE_PATH>".format(bcolors.WARNING,bcolors.ENDC))
            
    else:
        print("{}Usage: example <FILE_PATH>".format(bcolors.WARNING,bcolors.ENDC))

def get_options(d,options,id=False):
    for k,v in d.items():
        if id == True:
            options.append(k)
        elif k == menu_state:
            options = get_options(v,options,True)
        elif isinstance(v, dict):
            options = get_options(v,options)
    return options

def help(cmd=None):
    print("Command list:")
    options = get_options(menu_option,[])
    for option in options:
        print("{}[*] {}{}{}".format(bcolors.OKBLUE,bcolors.ENDC,bcolors.BOLD,option))

def state(cmd=None):
    global menu_state
    global completer
    menu_state = cmd[0]
    completer.update(get_options(menu_option,[]))

def exit(cmd=None):
    global menu_state
    menu_state = "exit"

def invalid(cmds=None):
    print("{}Invalid Command! Use help for options.{}".format(bcolors.WARNING,bcolors.ENDC))

def get_parent(d,t):
    out = t
    for k,v in d.items():
        if k == menu_state:
            return ("",True)
        elif isinstance(v, dict) and len(v) > 0:
            tmp = get_parent(v,t)
            if tmp[0] == "" and tmp[1] == True:
                return (k,True)
            else:
                out = tmp
        else:
            return t
    return out

def back(cmd=None):
    global menu_state
    menu_state = get_parent(menu_option,("",False))[0]

def parse(cmd):
    values = cmd.split()
    switcher_menu[menu_state].get(values[0], invalid)(values)
    return menu_state

# MENU OPTIONS VALUES
menu_option = {
                    "main": {
                        "example" : {
                            "edit":{},
                            "help":{},
                            "back":{}
                            },
                        "help":{},
                        "exit":{}
                        }
                  }
switcher_menu = {"main":{"exit":exit,"help":help,"example":state},"example":{"edit":example,"help":help,"back":back}}
menu_state   = "main"

# LOAD SETTINGS
config = Config()

# AUTOCOMPLETE SETUP
completer = Completer(get_options(menu_option,[]))
readline.set_completer(completer.complete)
readline.parse_and_bind('tab: complete')

# BANNER
print("{}{}{}".format(bcolors.HEADER,pyfiglet.figlet_format(config.name),bcolors.ENDC))
