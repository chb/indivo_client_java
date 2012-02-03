from __future__ import print_function
import xml.dom.minidom
import xml.dom
import api_skeleton

def process_dom(apidom):
    docel = apidom.documentElement
    assert docel.tagName == "api"
    mdict = {}
    
    for child in docel.childNodes:
        if child.nodeType not in [xml.dom.Node.TEXT_NODE, xml.dom.Node.COMMENT_NODE]:
            if child.nodeType != xml.dom.Node.ELEMENT_NODE:
                print("nodetype: " + str(child.nodeType))
                raise Exception
            assert child.tagName == "method"
            process_method(child, mdict)
    
    for httmurl, mnames in mdict.items():
        if len(mnames) > 1:
            print(repr(httmurl) + " --> " + repr(mnames))

#    print("PYTHON_CLIENT_NAMES = {")
#    first = True
#    for httmurl, mnames in mdict.items():
#        if first:
#            first = False
#        else:
#            print(",")
#        print("(\"" + httmurl[0] + "\", \"" + httmurl[1] + "\") : \"" + mnames[0] + '"', end='')
#    print('\n}')
    
    return mdict
    

def process_method(methodNode, mdict):
    calls = methodNode.getElementsByTagName("call")
    assert calls.length == 1
    thecall = calls.item(0)
    mname = str(thecall.getAttribute("name"))
    httpmeth = str(thecall.getAttribute("method"))
    murl  = str(thecall.getAttribute("url"))
    qix = murl.find('?')
    if qix != -1:
        assert murl[qix +1:] == "{PARAMETERS}"
        murl = murl[:qix]
        
    #print(mname, httpmeth, murl)
    fromdict = mdict.get((httpmeth,murl))
    if fromdict:
        fromdict.append(mname)
    else:
        mdict[(httpmeth,murl)] = [mname]

if __name__ == "__main__":
    apidom = xml.dom.minidom.parse("../xml/api.xml")
    process_dom(apidom)
    apidom.unlink()
