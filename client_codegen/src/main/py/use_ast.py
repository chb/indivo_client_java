import ast
import glob

def areport(apath):
    pyf = open(apath)
    labcode = pyf.read()
    myast = ast.parse(labcode)
    
    idofinterest = None
    retval = []
    
    for bodyel in myast.body:
        if bodyel.__class__.__name__ == "FunctionDef":
            for decl in bodyel.decorator_list:
                if hasattr(decl, "func"):
                    print(decl.func.id)
                if hasattr(decl, "func") and  decl.func.id == "marsloader":
                    print(decl.func.id)
                    for bodyelel in bodyel.body:
                        if (bodyelel.__class__.__name__ == "Assign"
                             and hasattr(bodyelel.value.func, "id")
                              and bodyelel.value.func.id == "FactQuery"):
                            #print(repr(bodyelel.value.args[1].id))
                            idofinterest = bodyelel.value.args[1].id 
    
    if (idofinterest):
        #print("idofinterest: " + idofinterest)
        for bodyel in myast.body:
            if bodyel.__class__.__name__ == "Assign":
                if (len(bodyel.targets) == 1
                     and bodyel.targets[0].id == idofinterest
                      and str(bodyel.value.__class__.__name__) == "Dict"):
                    zipped = zip(bodyel.value.keys, bodyel.value.values)
                    for azip in zipped:
                        if azip[0].__class__.__name__ == "Str":
                            #print(azip[0].s + " -- " + azip[1].elts[1].id)
                            retval.append((azip[0].s, azip[1].elts[1].id))
    return idofinterest, retval

def all_reports(directory):
    reportpaths = glob.glob(directory + "/indivo/views/reports/*.py")
    if not reportpaths:
        print("not an indivo_server root path: " + directory)
        raise Exception
    retval = {}
    for apath in reportpaths:
        print(apath)
        idofinterest, allowedflds = areport(apath)
        if allowedflds:
            retval[idofinterest] = allowedflds
#        for afld, acls in allowedflds:
#            print("field: " + afld + "   " + "class: " + acls)
#    print("\n\n\n")
#    print(repr(retval))
    return retval

#if __name__ == "__main__":
#    all_reports("/home/nate/indivo/github")
